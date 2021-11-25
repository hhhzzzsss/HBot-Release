package com.github.hhhzzzsss.hbot.commands;

import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.processes.treegen.TreeGenProcess;
import com.github.hhhzzzsss.hbot.processes.treegen.TreeType;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class TreeGenCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "treegen";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"list - lists tree types",
			"[flags] <type> <x> <y> <z>",
			"-bare ... - no leaves",
			"-hollow ... - no interior",
			"-fillair ... - fills air",
			"-hcentered ... - horizontally centered",
			"-vcentered ... - vertically centered",
			"-centered ... - horizontally and vertically centered",
			"-fillair ... - fills air",
		};
	}

	@Override
	public String getDescription() {
		return "Generates trees with a space-colonization algorithm";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Schematic"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		if (args.toLowerCase().startsWith("list")) {
    		int color = 0;
    		StringBuilder sb = new StringBuilder("&2Tree Types -");
    		for (TreeType type : TreeType.values()) {
    			String colorCode;
    			if (color == 0) colorCode = "&9";
    			else colorCode = "&b";
    			sb.append(" " + colorCode + type.name());
    			color = 1 - color;
    		}
			platform.sendMessage(sb.toString());
		}
		else {
			ArgsParser parser = new ArgsParser(this, args);
			List<String> flags = parser.readFlags();
			TreeType type = parser.readEnum(TreeType.class, true);
			int x = parser.readInt(true);
			int y = parser.readInt(true);
			int z = parser.readInt(true);
			hbot.getCommandCore().checkProcessFree();
			hbot.getCommandCore().setProcess(new TreeGenProcess(hbot, platform, x, y, z, type, flags));
		}
	}
}
