package com.github.hhhzzzsss.hbot.commands;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class PlayerFilterCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "playerfilter";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"set <regex>",
			"get",
			"off",
		};
	}

	@Override
	public String getDescription() {
		return "Filters players based on a regex";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		execute(parser, PlatformInfo.getMinecraft(hbot));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		execute(parser, PlatformInfo.getDiscord(hbot, event, "Player Filter"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("set")) {
			String filter = parser.readString(true).replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			try {
				hbot.getBlacklistManager().setPlayerFilterPattern(Pattern.compile(filter));
			}
			catch (PatternSyntaxException e) {
				throw new CommandException("Invalid regex");
			}
			platform.sendMessage("&7Set filter regex to &3" + filter);
		}
		else if (subCommand.equalsIgnoreCase("get")) {
			if (hbot.getBlacklistManager().getPlayerFilterPattern() == null) {
				throw new CommandException("There is currently no player filter in use");
			}
			else {
				platform.sendMessage("&7The current player filter is &3" + hbot.getBlacklistManager().getPlayerFilterPattern().toString());
			}
		}
		else if (subCommand.equalsIgnoreCase("off")) {
			if (hbot.getBlacklistManager().getPlayerFilterPattern() == null) {
				throw new CommandException("There is currently no player filter in use");
			}
			else {
				hbot.getBlacklistManager().setPlayerFilterPattern(null);
				platform.sendMessage("&7Removed player filter");
			}
		}
		else {
			throw new CommandException("Invalid subcommand");
		}
	}
}
