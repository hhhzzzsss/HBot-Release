package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.modules.BlacklistManager;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class BlacklistCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "blacklist";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"add <username>",
			"remove <username>",
			"list",
			"clear",
		};
	}

	@Override
	public String getDescription() {
		return "Adds, removes, lists, or clears players in the blacklist";
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
		execute(parser, PlatformInfo.getDiscord(hbot, event, "Blacklist"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("add")) {
			String username = parser.readString(true).replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			hbot.getBlacklistManager().add(username);
			platform.sendMessage("&7Added &3" + username + " &7to the blacklist");
		}
		else if (subCommand.equalsIgnoreCase("remove")) {
			String username = parser.readString(true).replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			hbot.getBlacklistManager().remove(username);
			platform.sendMessage("&7Removed &3" + username + " &7from the blacklist");
		}
		else if (subCommand.equalsIgnoreCase("list")) {
			BlacklistManager.readBlacklist();
			StringBuilder sb = new StringBuilder("&7Blacklisted players -");
			boolean color = false;
			for (String username : BlacklistManager.getBlacklist()) {
				sb.append(color ? " &9" : " &b");
				color = !color;
				sb.append(username);
			}
			platform.sendMessage(sb.toString());
		}
		else if (subCommand.equalsIgnoreCase("clear")) {
			hbot.getBlacklistManager().clear();
			platform.sendMessage("&7Cleared the blacklist");
		}
		else {
			throw parser.getGenericError();
		}
	}
}
