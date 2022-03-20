package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.util.BlockUtils;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class DebugCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"getblock <x> <y> <z>",
		};
	}

	@Override
	public String getDescription() {
		return "Debug tools";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Debug"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("getblock")) {
			int x = parser.readInt(true);
			int y = parser.readInt(true);
			int z = parser.readInt(true);
			int blockState = hbot.getWorld().getBlock(x, y, z);
			platform.sendMessage(String.format("&7The detected block at &3%d %d %d &7is &3%s &b(block state=%d)", x, y, z, BlockUtils.getBlockByStateId(blockState).getDisplayName(), blockState));
			System.out.println("Is air: " + BlockUtils.isAir(blockState));
		}
		else {
			throw parser.getGenericError();
		}
	}
}
