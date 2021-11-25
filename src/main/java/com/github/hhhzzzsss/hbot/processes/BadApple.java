package com.github.hhhzzzsss.hbot.processes;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import org.apache.commons.io.IOUtils;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.modules.music.MidiConverter;
import com.github.hhhzzzsss.hbot.modules.music.Song;

public class BadApple extends CoreProcess {
	private final HBot hbot;
	private final Song song;
	private final String[] video;
	private int tick = 0;
	
	public BadApple(HBot hbot) throws CommandException {
		this.hbot = hbot;
		
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("badapple/badapple.mid");
			Sequence sequence = MidiSystem.getSequence(is);
			song = MidiConverter.getSong(sequence, "badapple.mid");
		} catch (InvalidMidiDataException | IOException e) {
			throw new CommandException("Error reading midi");
		}
		
		try {
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("badapple/badapple.txt");
			String str = IOUtils.toString(is, "utf-8");
			video = str.replaceAll("\\r?\\n", "\\\\n").split("\\\\n--\\\\n");
		} catch (IOException e) {
			throw new CommandException("Error reading video");
		}
	}
	
	@Override
	public void onTick() {
		if (song.isFinished() && tick >= video.length*2) {
			stop();
			return;
		}
		
		if (tick % 2 == 0 && tick < video.length*2) {
			hbot.getCommandCore().run(String.format("tellraw @a [\"%s\"]", video[tick/2]));
		}
		
		while (!song.isFinished() && song.reachedNextNote()) {
			hbot.getCommandCore().run(song.nextCommand());
		}
		
		tick++;
		song.advanceTick();
	}
}
