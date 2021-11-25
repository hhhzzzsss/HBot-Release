package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class CoreCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "core";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"size <size>",
			"speed <speed>",
			"reset",
			"cancel",
		};
	}

	@Override
	public String getDescription() {
		return "Configures command core";
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
		execute(args, PlatformInfo.getDiscord(hbot, event, "Core Settings"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String subCommand = parser.readWord(true);
		if (subCommand.equalsIgnoreCase("size")) {
			sizeCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("speed")) {
			speedCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("reset")) {
			resetCommand(parser, platform);
		}
		else if (subCommand.equalsIgnoreCase("cancel")) {
			cancelCommand(parser, platform);
		}
		else {
			throw parser.getGenericError();
		}
	}
	
	private void sizeCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		int size = parser.readInt(true);
		if (size < 1 || size > 16) {
			throw parser.getCustomError("Must be an integer from 1 to 16");
		}
		hbot.getCommandCore().setCoreHeight(size);
		platform.sendMessage("&7Core size set to &3" + size);
	}
	
	private void speedCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		int speed = parser.readInt(true);
		if (speed < 1 || speed > 100) {
			throw parser.getCustomError("Must be an integer from 1 to 100");
		}
		hbot.getCommandCore().setCommandSpeed(speed);
		platform.sendMessage("&7Core speed set to &3" + speed);
	}
	
	private void resetCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		hbot.getCommandCore().setCommandLag(0);
		platform.sendMessage("&7Reset lag detector");
	}
	
	private void cancelCommand(ArgsParser parser, PlatformInfo platform) throws CommandException {
		if (hbot.getCommandCore().getProcess() != null) {
			hbot.getCommandCore().getProcess().stop();
			platform.sendMessage("&7Core process canceled");
		}
		else {
			platform.sendMessage("&7There is no core process in progress");
		}
	}
}
