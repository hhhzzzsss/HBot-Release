package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@RequiredArgsConstructor
public class MsgFilterCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "msgfilter";
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
		return "Deops and mutes players that say a chat message matching a regex (Note that this matches the entire chat message, including the prefix and username part)";
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
				hbot.getBlacklistManager().setMessageFilterPattern(Pattern.compile(filter));
			}
			catch (PatternSyntaxException e) {
				throw new CommandException("Invalid regex");
			}
			platform.sendMessage("&7Set filter regex to &3" + filter);
		}
		else if (subCommand.equalsIgnoreCase("get")) {
			if (hbot.getBlacklistManager().getMessageFilterPattern() == null) {
				throw new CommandException("There is currently no player filter in use");
			}
			else {
				platform.sendMessage("&7The current player filter is &3" + hbot.getBlacklistManager().getMessageFilterPattern().toString());
			}
		}
		else if (subCommand.equalsIgnoreCase("off")) {
			if (hbot.getBlacklistManager().getMessageFilterPattern() == null) {
				throw new CommandException("There is currently no player filter in use");
			}
			else {
				hbot.getBlacklistManager().setMessageFilterPattern(null);
				platform.sendMessage("&7Removed player filter");
			}
		}
		else {
			throw new CommandException("Invalid subcommand");
		}
	}
}
