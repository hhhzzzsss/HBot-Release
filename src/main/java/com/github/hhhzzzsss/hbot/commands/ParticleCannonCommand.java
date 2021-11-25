package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.entity.EntitySelector;
import com.github.hhhzzzsss.hbot.processes.ParticleCannon;
import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class ParticleCannonCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "particlecannon";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<entityType> <particleType> [speed] [maxTargets]"};
	}

	@Override
	public String getDescription() {
		return "Shoots particles at nearby entities";
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
		execute(args, PlatformInfo.getDiscord(hbot, event, "Sentry"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		EntitySelector entitySelector = parser.readEntitySelector(true);
		String particle = parser.readWord(true);
		Double speed = parser.readDouble(false);
		Integer maxTargets = parser.readInt(false);
		if (speed == null) speed = 1.0;
		if (maxTargets == null) maxTargets = 3;
		
		if (hbot.getCommandCore().getProcess() == null || hbot.getCommandCore().getProcess() instanceof ParticleCannon) {
			hbot.getCommandCore().forceSetProcess(new ParticleCannon(hbot, entitySelector, particle, speed, maxTargets));
			hbot.sendChat("&7Enabled particle cannon");
		}
		else {
			throw new CommandException("Another process is already running");
		}
	}
}
