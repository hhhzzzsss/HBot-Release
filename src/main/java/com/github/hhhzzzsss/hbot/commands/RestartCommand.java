package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.Main;
import com.github.hhhzzzsss.hbot.command.*;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class RestartCommand implements ChatCommand, GlobalDiscordCommand {
	private final Bot bot;

	@Override
	public String getName() {
		return "restart";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Restarts the bot";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		bot.sendChatInstantly("Restarting...");
		new Thread(() -> {
			Main.restart();
		}).start();
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		bot.sendChatInstantly("Restarting...");
		new Thread(() -> {
			Main.restart();
		}).start();
	}
}
