package com.github.hhhzzzsss.hbot.commands;

import java.util.UUID;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class UUIDCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "uuid";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"[player]"};
	}

	@Override
	public String getDescription() {
		return "Tries to get the uuid of you or another player";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		PlatformInfo platform = PlatformInfo.getMinecraft(hbot);
		ArgsParser parser = new ArgsParser(this, args);
		String username = parser.readString(false);
		if (username == null) username = sender;
		else username = username.replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
		UUID uuid = hbot.getPlayerListTracker().getRecordedUUID(username);
		if (uuid == null) {
			throw new CommandException("Could not get uuid of " + username.replace("§", "&"));
		}
		platform.sendMessage("&7The UUID of &3" + username + " &7is &3" + uuid.toString());
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		PlatformInfo platform = PlatformInfo.getDiscord(hbot, event, "UUID");
		ArgsParser parser = new ArgsParser(this, args);
		String username = parser.readString(true).replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
		UUID uuid = hbot.getPlayerListTracker().getRecordedUUID(username);
		if (uuid == null) {
			throw new CommandException("Could not get uuid of " + username);
		}
		platform.sendDiscordOnlyMessage("The UUID of " + username + " is " + uuid.toString());
	}
}
