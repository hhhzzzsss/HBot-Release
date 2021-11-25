package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.processes.SchemProcess;
import com.github.hhhzzzsss.hbot.processes.mapart.MapartLoaderThread;
import com.github.hhhzzzsss.hbot.processes.mapart.MapartProcess;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class MapartCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "mapart";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"<x> <z> <image url>",
		};
	}

	@Override
	public String getDescription() {
		return "Makes mapart of the specified image";
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
		execute(parser, PlatformInfo.getDiscord(hbot, event, "Mapart"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		List<String> flags = parser.readFlags();
		int x = parser.readInt(true);
		int z = parser.readInt(true);
		String url = parser.readString(true);
		hbot.getCommandCore().setProcess(new MapartProcess(hbot, x, z, url, flags));
		platform.sendMessage("&7Creating mapart for &3" + url);
	}
}
