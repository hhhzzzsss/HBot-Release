package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;
import java.io.File;
import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.processes.SchemProcess;
import com.github.hhhzzzsss.hbot.processes.boxstructure.BoxStructure;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class BoxStructureCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "boxstructure";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"<x> <y> <z> [seed]",
		};
	}

	@Override
	public String getDescription() {
		return "Generates a random box structure";
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
		execute(parser, PlatformInfo.getDiscord(hbot, event, "Box Structure"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		int x = parser.readInt(true);
		int y = parser.readInt(true);
		int z = parser.readInt(true);
		Integer seed = parser.readInt(false);
		if (seed == null) {
			hbot.getCommandCore().setProcess(new BoxStructure(hbot, x, y, z));
		}
		else {
			hbot.getCommandCore().setProcess(new BoxStructure(hbot, x, y, z, seed));
		}
		platform.sendMessage(String.format("&7Now building a new box structure centered at &3%d &3%d &3%d ", x, y, z));
	}
}
