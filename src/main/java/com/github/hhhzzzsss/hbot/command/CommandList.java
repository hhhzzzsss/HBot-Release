package com.github.hhhzzzsss.hbot.command;

import java.util.Collection;
import java.util.HashMap;

import lombok.*;

@NoArgsConstructor
public class CommandList {
	private HashMap<String, Command> commands = new HashMap<>();
	
	public void add(Command command) {
		commands.put(command.getName().toLowerCase(), command);
	}
	
	public Command get(String name) {
		return commands.get(name);
	}
	
	public boolean contains(String name) {
		return commands.containsKey(name);
	}
	
	public Collection<Command> getCommands() {
		return commands.values();
	}
}
