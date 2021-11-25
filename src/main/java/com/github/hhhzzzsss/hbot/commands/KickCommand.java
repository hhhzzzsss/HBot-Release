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

// This command is now patched, so I removed it
@RequiredArgsConstructor
public class KickCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<player>"};
	}

	@Override
	public String getDescription() {
		return "Kicks a player from the server";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Kick"));
	}
	
	private void execute(String args, PlatformInfo  platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String player = parser.readString(true);
		UUID uuid;
		try {
			uuid = UUID.fromString(player);
		}
		catch(IllegalArgumentException|NullPointerException e) {
			player = player.replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			uuid = hbot.getPlayerListTracker().getRecordedUUID(player);
		}
		if (uuid == null) {
			throw new CommandException("Could not find player " + player);
		}
		hbot.getCommandCore().run("execute as " + uuid + " run title @s[type=player] title [{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"},{\"sel\\u0065ctor\":\"@e\"}]");
	}
}
