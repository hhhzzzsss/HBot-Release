package com.github.hhhzzzsss.hbot.command;

public interface ChatCommand extends Command {
	public void executeChat(String sender, String args) throws CommandException;
}
