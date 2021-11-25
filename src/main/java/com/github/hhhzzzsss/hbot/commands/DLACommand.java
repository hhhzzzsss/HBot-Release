package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.BlockSelector;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.processes.SchemProcess;
import com.github.hhhzzzsss.hbot.processes.dla.BrownianProcess;
import com.github.hhhzzzsss.hbot.processes.dla.DLAProcess;
import com.github.hhhzzzsss.hbot.processes.dla.SkyFallProcess;
import com.github.hhhzzzsss.hbot.processes.mapart.MapartLoaderThread;
import com.github.hhhzzzsss.hbot.processes.mapart.MapartProcess;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class DLACommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "dla";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"skyfall <materials> [sticky blocks] [particles]",
			"brownian <materials> [sticky blocks] [particle percent]"
		};
	}

	@Override
	public String getDescription() {
		return "Creates diffusion limited aggregation fractals seeded by the terrain";
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
		execute(parser, PlatformInfo.getDiscord(hbot, event, "DLA"));
	}
	
	private void execute(ArgsParser parser, PlatformInfo platform) throws CommandException {
		String type = parser.readWord(true);
		if (type.equalsIgnoreCase("skyfall")) {
			String materials = parser.readWord(true);
			BlockSelector selector = parser.readBlockSelector(false);
			if (selector == null) selector = new BlockSelector("all");
			Integer iterations = parser.readInt(false);
			if (iterations == null) iterations = 100000;
			if (iterations > 1000000) {
				throw parser.getCustomError("iterations cannot exceed 1000000");
			}
			hbot.getCommandCore().setProcess(new SkyFallProcess(hbot, materials.split(","), selector, iterations));
			platform.sendMessage("&7Creating diffusion-limited aggregation...");
		}
		else if (type.equalsIgnoreCase("brownian")) {
			String materials = parser.readWord(true);
			BlockSelector selector = parser.readBlockSelector(false);
			if (selector == null) selector = new BlockSelector("all");
			Double percent = parser.readDouble(false);
			if (percent == null) percent = 1.0;
			if (percent > 100.0) {
				throw parser.getCustomError("Percent cannot exceed 100");
			}
			hbot.getCommandCore().setProcess(new BrownianProcess(hbot, materials.split(","), selector, percent / 100.0));
			platform.sendMessage("&7Creating diffusion-limited aggregation...");
		}
		else {
			throw parser.getGenericError();
		}
	}
}
