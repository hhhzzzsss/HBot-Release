package com.github.hhhzzzsss.hbot.command;

public interface Command {
	public String getName();
	public String[] getSyntax();
	public String getDescription();
	public int getPermission();
}
