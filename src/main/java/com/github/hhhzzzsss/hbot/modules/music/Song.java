package com.github.hhhzzzsss.hbot.modules.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import lombok.*;

public class Song implements Iterable<Note> {
	@Getter private String name;
	
	private ArrayList<Note> notes = new ArrayList<>();
	@Getter private int position = 0;
	@Getter @Setter private boolean loop = false;
	@Getter @Setter private int loopPosition = -1000;
	@Getter private int time = 0;
	
	public Song(String name) {
		this.name = name;
	}
	
	public void addNote(Note note) {
		notes.add(note);
	}
	
	public void sort() {
		Collections.sort(notes);
	}
	
	public boolean reachedNextNote() {
		return notes.get(position).getTime() <= this.time;
	}
	
	public String nextCommand() {
		if (isFinished()) {
			return null;
		}
		
		Note nextNote = notes.get(position);
		position++;
		if (loop && isFinished()) {
			setTime(loopPosition);
		}
		//return "/playsound " + nextNote.getMinecraftSound() + " record @a 0 30000000000000 0 300000000000000000000000000000000000000 " + nextNote.getFloatingPitch();
		return "/playsound " + nextNote.getMinecraftSound() + " record @a 0 30000000000000 0 0 " + nextNote.getFloatingPitch() + " " + nextNote.getFloatingVolume();
	}
	
	public int totalTime() {
		return notes.get(notes.size()-1).getTime();
	}
	
	public int getTick() {
		return time / 50;
	}
	
	public boolean isFinished() {
		return position >= notes.size();
	}
	
	public void advanceTick() {
		time += 50;
	}
	
	public void setTime(int millis) {
		time = millis;
		position = 0;
		while (!isFinished() && notes.get(position).getTime() < millis) {
			position++;
		}
	}
	
	public String getStatusCommand() {
		String curTimeStamp = getTimeStamp(time);
		String totTimeStamp = getTimeStamp(totalTime());
		String content = String.format("§6Now Playing §8| §3§l%s §8| §b%s §7/ §b%s", name, curTimeStamp, totTimeStamp);
		if (loop) content += " §8| §6Looping enabled";
		return String.format("title @a actionbar \"%s\"", content.replace("\"", "\\\""));
	}
	
	private static String getTimeStamp(long millis) {
		int seconds = (int) (Math.max(millis, 0) / 1000);
		int minutes = seconds / 60;
		seconds %= 60;
		return String.format("%d:%02d", minutes, seconds);
	}
	
	@Override
	public Iterator<Note> iterator() {
		return notes.iterator();
	}
}
