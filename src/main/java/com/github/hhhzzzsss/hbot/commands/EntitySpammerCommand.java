package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.processes.EntitySpammer;
import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class EntitySpammerCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "entityspammer";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<entityType> <x> <y> <z> [spread] [nbt]"};
	}

	@Override
	public String getDescription() {
		return "Spawns a ton of entities near a specified point";
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
		execute(args, PlatformInfo.getDiscord(hbot, event, "Entity Spammer"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		
		String entity = parser.readWord(true);
		
		double x = parser.readDouble(true);
		double y = parser.readDouble(true);
		double z = parser.readDouble(true);
		
		Double spread = parser.readDouble(false);
		
		String nbt = null;
		if (spread != null) nbt = parser.readString(false);
		
		if (spread==null) spread = 3.0;
		if (nbt == null) nbt = "";
		
		if (hbot.getCommandCore().getProcess() == null || hbot.getCommandCore().getProcess() instanceof EntitySpammer) {
			hbot.getCommandCore().forceSetProcess(new EntitySpammer(hbot, entity, x, y, z, spread, nbt));
			hbot.sendChat("&7Enabled entity spammer");
		}
		else {
			throw new CommandException("Another process is already running");
		}
	}
}
