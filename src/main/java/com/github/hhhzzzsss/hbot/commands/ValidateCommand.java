package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.ChatSender;
import com.github.hhhzzzsss.hbot.command.CommandException;

import lombok.*;

@RequiredArgsConstructor
public class ValidateCommand implements ChatCommand {
	private final Bot bot;

	@Override
	public String getName() {
		return "validate";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"[string]"};
	}

	@Override
	public String getDescription() {
		return "Tests hash verification";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		bot.sendChat("&aValid Hash");
	}
}
