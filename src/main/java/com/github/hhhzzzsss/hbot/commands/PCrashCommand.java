package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.processes.PCrashProcess;
import com.github.hhhzzzsss.hbot.util.HashUtils;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class PCrashCommand implements ChatCommand, DiscordCommand {
	
	private final HBot hbot;

	@Override
	public String getName() {
		return "pcrash";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"pos <x> <y> <z>",
			"player <player>",
		};
	}

	@Override
	public String getDescription() {
		return "Makes players lag out and crash";
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
		execute(args, PlatformInfo.getDiscord(hbot, event, "PCrash"));
	}
	
	private void execute(String args, PlatformInfo  platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		
		String subCommand = parser.readWord(true);
		
		if (subCommand.equalsIgnoreCase("pos")) {
			double x = parser.readDouble(true);
			double y = parser.readDouble(true);
			double z = parser.readDouble(true);
			hbot.getCommandCore().setProcess(new PCrashProcess(hbot, x, y, z));
			platform.sendMessage(String.format("&7Enabled PCrash at &3%.02f %.02f %.02f", x, y, z));
		}
		else if (subCommand.equalsIgnoreCase("player")) {
			String username = parser.readString(true).replaceAll("(?<!\\\\)%", "§").replace("\\%", "%");
			hbot.getCommandCore().run(String.format("/execute as %s at @s run particle minecraft:dust_color_transition 1 0 0 2 0 1 0 ~ ~1.5 ~ 0.1 0.1 0.1 0 2147483646 force @s", HashUtils.getOfflineUUID(username)));
			platform.sendMessage(String.format("&7Attempting to crash &3%s", username));
		}
		else {
			throw parser.getGenericError();
		}
	}
}
