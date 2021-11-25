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
public class LoginNameCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "loginname";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"[player]"};
	}

	@Override
	public String getDescription() {
		return "Tries to get the login name of you or another player";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		PlatformInfo platform = PlatformInfo.getMinecraft(hbot);
		ArgsParser parser = new ArgsParser(this, args);
		String player = parser.readString(false);
		UUID uuid;
		try {
			uuid = UUID.fromString(player);
		}
		catch(IllegalArgumentException|NullPointerException e) {
			if (player == null) player = sender;
			player = player.replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			uuid = hbot.getPlayerListTracker().getRecordedUUID(player);
		}
		String loginName = uuid == null ? null : hbot.getPlayerListTracker().getRecordedLoginName(uuid);
		if (loginName == null) {
			throw new CommandException("Could not get login name of " + player.replace("§", "&"));
		}
		platform.sendMessage("&7The login name of &3" + player + " &7is &3" + loginName);
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		PlatformInfo platform = PlatformInfo.getDiscord(hbot, event, "Login Name");
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
		String loginName = uuid == null ? null : hbot.getPlayerListTracker().getRecordedLoginName(uuid);
		if (loginName == null) {
			throw new CommandException("Could not get login name of " + player);
		}
		platform.sendDiscordOnlyMessage(String.format("The login name of &3%s &7is &3%s", player, loginName));
	}
}
