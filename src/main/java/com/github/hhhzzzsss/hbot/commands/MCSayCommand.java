package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class MCSayCommand implements DiscordCommand {
	private final Bot bot;

	@Override
	public String getName() {
		return "mcsay";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<text>"};
	}

	@Override
	public String getDescription() {
		return "Says something in Minecraft";
	}

	@Override
	public int getPermission() {
		return 1;
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String message = parser.readString(true);
		if (message.startsWith("/")) {
			bot.sendCommand(message);
		}
		else {
			bot.sendChat(message);
		}
	}
}
