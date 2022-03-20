package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.entity.PlayerData;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class ListCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Lists players";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		execute(PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(PlatformInfo.getDiscord(hbot, event, "Player List"));
	}
	
	private void execute(PlatformInfo platform) throws CommandException {
		StringBuilder sb = new StringBuilder();
		for (PlayerData data : hbot.getPlayerListTracker().getPlayerList().values()) {
			sb.append(String.format("&b%s &7> &3%s\n", data.getName(), data.getUUID().toString()));
		}
		if (sb.length() > 0) sb.deleteCharAt(sb.length()-1); // remove trailing newline
		platform.sendResponseOnlyMessage(sb.toString());
	}
}
