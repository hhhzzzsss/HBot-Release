package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.processes.BadApple;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class BadAppleCommand implements ChatCommand, DiscordCommand {
	
	private final HBot hbot;

	@Override
	public String getName() {
		return "badapple";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Nagareteku toki no naka de demo...";
	}

	@Override
	public int getPermission() {
		return 1;
	}
	
	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Bad Apple"));
	}
	
	private void execute(String args, PlatformInfo  platform) throws CommandException {
		hbot.getCommandCore().setProcess(new BadApple(hbot));
		platform.sendMessage("&7Starting Bad Apple...");
	}
}
