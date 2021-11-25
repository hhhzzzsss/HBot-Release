package com.github.hhhzzzsss.hbot.modules.music;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
public class Note implements Comparable<Note> {
	private Instrument instrument;
	private int pitch;
	private float volume;
	private int time;
	
	public String getMinecraftSound() {
		return "block.note_block."+instrument.name().toLowerCase();
	}
	
	public float getFloatingPitch() {
		return (float) Math.pow(2, (pitch-12) / 12.0);
	}
	
	public float getFloatingVolume() {
		return volume;
	}
	
	@Override
	public int compareTo(Note other) {
		if (time < other.getTime()) {
			return -1;
		}
		else if (time > other.getTime()) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
