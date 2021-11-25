package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.entity.EntitySelector;
import com.github.hhhzzzsss.hbot.processes.Sentry;
import com.github.hhhzzzsss.hbot.processes.Sentry.AttackType;
import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class SentryCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "sentry";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"list - lists attack types",
			"<entityType> <attackType> [maxTargets]",
		};
	}

	@Override
	public String getDescription() {
		return "Launches attacks at nearby entities.";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args, PlatformInfo.getMinecraft(hbot, hbot.getCommandCore()));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args, PlatformInfo.getDiscord(hbot, event, "Sentry"));
	}
	
	private void execute(String args, PlatformInfo platform) throws CommandException {
		if (args.toLowerCase().startsWith("list")) {
    		int color = 0;
    		StringBuilder sb = new StringBuilder("&7Attack Types -");
    		for (AttackType type : Sentry.AttackType.values()) {
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
			EntitySelector entitySelector = parser.readEntitySelector(true);
			AttackType type = parser.readEnum(AttackType.class, true);
			Integer maxTargets = parser.readInt(false);
			if (maxTargets == null) maxTargets = 3;
			if (hbot.getCommandCore().getProcess() == null || hbot.getCommandCore().getProcess() instanceof Sentry) {
				hbot.getCommandCore().forceSetProcess(new Sentry(hbot, entitySelector, type, maxTargets));
				hbot.sendChat("&7Enabled sentry");
			}
			else {
				throw new CommandException("Another process is already running");
			}
		}
	}
}
