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

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class SchemCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "schem";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"list",
			"build [flags] <x> <y> <z> <filename>",
			"build -hcentered ... - horizontally centered",
			"build -vcentered ... - vertically centered",
			"build -centered ... - horizontally and vertically centered",
			"build -fillair ... - fills air",
		};
	}

	@Override
	public String getDescription() {
		return "Builds schematics";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		execute(parser, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		execute(parser, PlatformInfo.getDiscord(hbot, event, "Schematic"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		String subCommand = parser.readWord(true);
		
		if (subCommand.equals("list")) {
    		int color = 0;
    		StringBuilder sb = new StringBuilder("&7Schematics -");
    		for (File schemFile : SchemProcess.SCHEM_DIR.listFiles()) {
    			String colorCode;
    			if (color == 0) colorCode = "&9";
    			else colorCode = "&b";
    			String fileName = schemFile.getName();
    			sb.append(" " + colorCode + fileName);
    			color = 1 - color;
    		}
			platform.sendMessage(sb.toString());
		}
		if (subCommand.equals("build")) {
			List<String> flags = parser.readFlags();
			int x = parser.readInt(true);
			int y = parser.readInt(true);
			int z = parser.readInt(true);
			String filename = parser.readString(true);
			hbot.getCommandCore().setProcess(new SchemProcess(hbot, x, y, z, filename, flags));
			platform.sendMessage("&7Now building &3" + filename);
		}
	}
}
