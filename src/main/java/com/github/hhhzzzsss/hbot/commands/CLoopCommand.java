package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.modules.CommandLooper;
import com.github.hhhzzzsss.hbot.util.ChatUtils;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class CLoopCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "cloop";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"add <interval> <command>",
			"remove <index>",
			"list",
			"clear",
		};
	}

	@Override
	public String getDescription() {
		return "Loops commands with a specified interval";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Command Loop"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		CommandLooper looper = hbot.getCommandLooper();
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("add")) {
			int interval = parser.readInt(true);
			String command = parser.readString(true);
			looper.add(command, interval, interval);
			platform.sendMessage(String.format("&7Added command &3%s &7with an interval of &3%d", command, interval));
		}
		else if (subCommand.equalsIgnoreCase("remove")) {
			int index = parser.readInt(true);
			CommandLooper.LoopedCommand loopedCommand = looper.get(index);
			looper.remove(index);
			platform.sendMessage(String.format("&7Removed command &3%s", loopedCommand.command));
		}
		else if (subCommand.equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder("&7Looped Commands -");
			if (looper.numCommands() == 0) {
				sb.append("\n&7None");
			}
			else {
				for (int i=0; i<looper.numCommands(); i++) {
					CommandLooper.LoopedCommand loopedCommand = looper.get(i);
					sb.append(String.format("\n&7%d. %s &3(interval=%d)", i, loopedCommand.command, loopedCommand.interval));
				}
			}
			platform.sendResponseOnlyMessage(sb.toString());
		}
		else if (subCommand.equalsIgnoreCase("clear")) {
			looper.clear();
			platform.sendMessage("&7Cleared looped commands");
		}
		else {
			throw parser.getGenericError();
		}
	}
}
