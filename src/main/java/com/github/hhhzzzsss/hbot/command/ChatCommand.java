package com.github.hhhzzzsss.hbot.command;

public interface ChatCommand extends Command {
	public void executeChat(ChatSender sender, String args) throws CommandException;
}
