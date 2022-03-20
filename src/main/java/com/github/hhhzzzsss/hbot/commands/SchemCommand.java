package com.github.hhhzzzsss.hbot.commands;

import java.io.File;
import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.processes.schem.SchemProcess;

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
			"stop",
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
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
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
		
		if (subCommand.equalsIgnoreCase("list")) {
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
		else if (subCommand.equalsIgnoreCase("stop")) {
			if (hbot.getCommandCore().getProcess() instanceof SchemProcess) {
				hbot.getCommandCore().getProcess().stop();
				platform.sendMessage("&7Schematic build canceled");
			}
			else {
				platform.sendMessage("&7No schematic is being built");
			}
		}
		else if (subCommand.equalsIgnoreCase("build")) {
			List<String> flags = parser.readFlags();
			int x = parser.readInt(true);
			int y = parser.readInt(true);
			int z = parser.readInt(true);
			String filename = parser.readString(true);
			hbot.getCommandCore().setProcess(new SchemProcess(hbot, platform, x, y, z, filename, flags));
			platform.sendMessage("&7Loading &3" + filename + "&7...");
		}
	}
}
