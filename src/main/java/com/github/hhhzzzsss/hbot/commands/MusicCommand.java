package com.github.hhhzzzsss.hbot.commands;

import java.io.File;
import java.util.Queue;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.modules.music.MusicPlayer;
import com.github.hhhzzzsss.hbot.modules.music.Song;
import com.github.hhhzzzsss.hbot.modules.music.SongLoaderThread;
import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class MusicCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "music";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"play <url or song name>",
			"list",
			"showqueue",
			"loop",
			"goto <mm:ss>",
			"skip",
			"stop",
		};
	}

	@Override
	public String getDescription() {
		return "Plays music";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Music"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("play")) {
			playCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("list")) {
			listCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("showqueue")) {
			showqueueCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("loop")) {
			loopCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("goto")) {
			gotoCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("skip")) {
			skipCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("stop")) {
			stopCommand(parser, platform);
		}
		else {
			throw parser.getGenericError();
		}
	}
	
	private void playCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		String location = parser.readString(true);
		try {
			hbot.getMusicPlayer().play(location, platform);
		} catch (Exception e) {
			throw new CommandException(e.getMessage());
		}
	}
	
	private void listCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		int color = 0;
		StringBuilder sb = new StringBuilder("&6Songs -");
		for (File songFile : MusicPlayer.SONG_DIR.listFiles()) {
			String colorCode;
			if (color == 0) colorCode = "&e";
			else colorCode = "&6";
			String fileName = songFile.getName();
			sb.append(" " + colorCode + fileName);
			color = 1 - color;
		}
		platform.sendMessage(sb.toString());
	}
	
	private void showqueueCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		StringBuilder sb = new StringBuilder("&6Queued Songs: ");
		Queue<SongLoaderThread> songQueue = hbot.getMusicPlayer().getSongQueue();
		if (songQueue.size() == 0) {
			platform.sendMessage("&6No songs in queue");
		}
		else {
			int idx = 0;
			for (SongLoaderThread songLoader : songQueue) {
				sb.append(String.format("\n&6%d. &3%s", ++idx, songLoader.getLocation()));
			}
			String str = sb.toString();
			hbot.getCommandCore().run("bcraw " + str);
			platform.sendDiscordOnlyMessage(str);
		}
	}
	
	private void loopCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		Song currentSong = hbot.getMusicPlayer().getCurrentSong();
		if (currentSong == null) {
			throw new CommandException("No song is currently playing");
		}
		currentSong.setLoop(!currentSong.isLoop());
		platform.sendMessage(currentSong.isLoop() ? "&6Enabled looping" : "&6Disabled looping");
	}
	
	private void gotoCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		Song currentSong = hbot.getMusicPlayer().getCurrentSong();
		if (currentSong == null) {
			throw new CommandException("No song is currently playing");
		}
		String timestamp = parser.readString(true);
		try {
			String split[] = timestamp.split(":");
			int minutes = Integer.parseInt(split[0]);
			int seconds = Integer.parseInt(split[1]);
			int millis = (minutes*60+seconds) * 1000;
			if (millis < 0 || millis > currentSong.totalTime()) {
				throw new CommandException("Time stamp out of bounds");
			}
			currentSong.setTime(millis / 50 * 50); // snaps to tick
		}
		catch (ArrayIndexOutOfBoundsException|NumberFormatException e) {
			throw parser.getCustomError("Invalid timestamp");
		}
		platform.sendMessage("&6Set song position to &3" + timestamp);
	}
	
	private void skipCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		if (hbot.getMusicPlayer().noActiveSong()) {
			throw new CommandException("No song is currently playing");
		}
		platform.sendMessage("&6Skipping current song");
		hbot.getMusicPlayer().skip();
	}
	
	private void stopCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		if (hbot.getMusicPlayer().noActiveSong()) {
			throw new CommandException("No song is currently playing");
		}
		platform.sendMessage("&6Stopping music playback");
		hbot.getMusicPlayer().stop();
	}
}
