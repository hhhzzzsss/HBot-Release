package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class CBCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "cb";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<command>"};
	}

	@Override
	public String getDescription() {
		return "Runs a command in a command block";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String command = parser.readString(true);
		if (!command.toLowerCase().matches("/?(essentials:|extras:)?(e?sudo|e?nick(name)?|rank|prefix|tag) .?b?.?l?hbot .*")) {
			hbot.getCommandCore().run(command);
		}
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String command = parser.readString(true);
		if (!command.toLowerCase().matches("/?(essentials:|extras:)?(e?sudo|e?nick(name)?|rank|prefix|tag) .?b?.?l?hbot .*")) {
			hbot.getCommandCore().run(command);
		}
	}
}
