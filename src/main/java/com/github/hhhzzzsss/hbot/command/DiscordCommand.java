package com.github.hhhzzzsss.hbot.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface DiscordCommand extends Command {
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException;
}
