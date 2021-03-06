package com.github.hhhzzzsss.hbot.commands;

import java.util.UUID;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.ChatSender;
import com.github.hhhzzzsss.hbot.command.CommandException;

import lombok.*;

@RequiredArgsConstructor
public class OrbitCommand implements ChatCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "orbit";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Creates a bot that orbits you";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Getter @Setter private int maxBots = 3;
	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		UUID uuid = hbot.getPlayerListTracker().getRecordedUUID(sender.getName());
		if (uuid == null || !hbot.getPlayerListTracker().getPlayerList().containsKey(uuid)) {
			throw new CommandException("Could not find player " + sender.getName());
		}
		hbot.getOrbitManager().createOrbit(hbot, sender.getName(), uuid);
		hbot.sendChat("&7Creating a bot orbiting &3" + sender.getName());
	}
}
