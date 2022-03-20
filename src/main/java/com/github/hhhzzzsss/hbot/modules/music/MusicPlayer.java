package com.github.hhhzzzsss.hbot.modules.music;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.sound.midi.InvalidMidiDataException;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.listeners.TickListener;

import lombok.*;

@RequiredArgsConstructor
public class MusicPlayer implements TickListener {
	public static final File SONG_DIR = new File("songs");
	static {
		SONG_DIR.mkdirs();
	}
	
	private final HBot hbot;
	
	@Getter Queue<SongLoaderThread> songQueue = new LinkedList<>();
	@Getter private SongLoaderThread currentSongLoaderThread = null;
	@Getter private Song currentSong = null;
	
	/**
	 * Plays or queues a song with the music player
	 * 
	 * @param location The song's filename
	 * @param platform
	 * @throws IOException
	 * @throws InvalidMidiDataException
	 */
	public void play(String location, PlatformInfo platform) throws IOException, InvalidMidiDataException, CommandException {
		SongLoaderThread loaderThread = new SongLoaderThread(location); 
		if (noActiveSong()) {
			loadSong(loaderThread);
			platform.sendDiscordOnlyMessage("Playing `" + location + "`");
		}
		else if (songQueue.size() >= 100) {
			throw new CommandException("Cannot exceed max queue length of 100");
		}
		else {
			platform.sendMessage("&6Added &3" + location + " &6to the song queue");
			songQueue.add(loaderThread);
		}
	}
	
	/**
	 * Plays or queues a song with the music player
	 * 
	 * @param location The song's filename
	 * @throws IOException
	 * @throws InvalidMidiDataException
	 */
	public void play(String location) throws IOException, InvalidMidiDataException {
		SongLoaderThread loaderThread = new SongLoaderThread(location); 
		if (noActiveSong()) {
			loadSong(loaderThread);
		}
		else {
			hbot.sendChat("&6Added &3" + location + " &6to the song queue");
			songQueue.add(loaderThread);
		}
	}
	
	public void skip() {
		currentSongLoaderThread = null;
		currentSong = null;
		if (songQueue.size() > 0) {
			loadSong(songQueue.poll());
		}
	}
	
	public void stop() {
		songQueue.clear();
		currentSongLoaderThread = null;
		currentSong = null;
	}
	
	@Override
	public void onTick() {
		if (noActiveSong() && !songQueue.isEmpty()) {
			loadSong(songQueue.poll());
		}
		else if (currentSongLoaderThread != null) {
			checkLoaderThread();
		}
		else if (currentSong != null) {
			songTick();
		}
	}
	
	private void loadSong(SongLoaderThread loaderThread) {
		currentSongLoaderThread = loaderThread;
		hbot.sendChat("&6Loading &3" + loaderThread.getLocation());
		loaderThread.start();
	}
	
	private void checkLoaderThread() {
		if (!currentSongLoaderThread.isAlive()) {
			if (currentSongLoaderThread.getSong() != null) {
				currentSong = currentSongLoaderThread.getSong();
				currentSong.setTime(-1000);
				hbot.sendChat("&6Now playing &3" + currentSong.getName());
			}
			else if (currentSongLoaderThread.getException() != null) {
				hbot.sendChat("&cError while loading song: &4" + currentSongLoaderThread.getException().getMessage());
			}
			currentSongLoaderThread = null;
		}
	}
	
	long nextStatusUpdate = System.currentTimeMillis();
	private void songTick() {
		long curTime = System.currentTimeMillis();
		while (!currentSong.isFinished() && currentSong.reachedNextNote()) {
			hbot.getCommandCore().run(currentSong.nextCommand());
		}
		if (System.currentTimeMillis() >= nextStatusUpdate) {
			hbot.getCommandCore().run(currentSong.getStatusCommand());
			nextStatusUpdate = curTime + 500;
		}
		currentSong.advanceTick();
		if (currentSong.isFinished()) {
			hbot.sendChat("&6Finished playing &3" + currentSong.getName());
			currentSong = null;
			if (songQueue.size() > 0) {
				loadSong(songQueue.poll());
			}
		}
	}
	
	public boolean noActiveSong() {
		return currentSongLoaderThread == null && currentSong == null;
	}
}
