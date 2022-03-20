package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.ChatSender;
import com.github.hhhzzzsss.hbot.command.CommandException;

import lombok.*;

@RequiredArgsConstructor
public class StopOrbitCommand implements ChatCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "stoporbit";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Removes the bot orbiting you";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Getter @Setter private int maxBots = 3;
	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		hbot.getOrbitManager().removeOrbit(sender.getName());
		hbot.sendChat("&7Removing the bot orbiting &3" + sender.getName());
	}
}
