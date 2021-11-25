package com.github.hhhzzzsss.hbot.modules;

import java.util.ArrayList;
import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.listeners.TickListener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandLooper implements TickListener {
	
	@AllArgsConstructor
	public class LoopedCommand {
		public String command;
		public int interval;
		public int delay;
	}

	private final HBot hbot;
	@Getter private ArrayList<LoopedCommand> commands = new ArrayList<>();
	
	public void add(String command, int interval, int delay) {
		commands.add(new LoopedCommand(command, interval, delay));
	}
	
	public void remove(int index) {
		commands.remove(index);
	}
	
	public LoopedCommand get(int index) {
		return commands.get(index);
	}
	
	public int numCommands() {
		return commands.size();
	}
	
	public void clear() {
		commands.clear();
	}
	
	@Override
	public void onTick() {
		for (LoopedCommand lc : commands) {
			if (lc.delay <= 0) {
				hbot.getCommandCore().run(lc.command);
				lc.delay = lc.interval;
			}
			else {
				lc.delay--;
			}
		}
	}
}
