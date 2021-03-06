package com.github.hhhzzzsss.hbot.commands;

import java.util.UUID;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.ChatSender;
import com.github.hhhzzzsss.hbot.command.CommandException;
import lombok.*;

@RequiredArgsConstructor
public class RtpCommand implements ChatCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "rtp";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Randomly teleports you";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		int x = (int)((Math.random()*2.-1.) * 1000000);
		int z = (int)((Math.random()*2.-1.) * 1000000);
		hbot.getCommandCore().run(String.format("tp %s %d 100 %d", sender.getUuid().toString(), x, z));
		hbot.sendCommand(String.format("&7Teleporting &3%s &7to &3%d 100 %d", sender.getName(), x, z));
	}
}
