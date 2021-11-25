package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.discord.DiscordNickManager;
import com.github.hhhzzzsss.hbot.discord.DiscordUtils;

import lombok.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class DiscordNickCommand implements DiscordCommand {
	@Override
	public String getName() {
		return "discordnick";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"<nick>",
			"off",
		};
	}

	@Override
	public String getDescription() {
		return "Sets your nick for the Discord->Minecraft bridge";
	}

	@Override
	public int getPermission() {
		return 1;
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String nick = parser.readString(true);
		Member member = event.getMember();
		if (nick.equalsIgnoreCase("off")) {
			if (DiscordNickManager.hasNick(member)) {
				DiscordNickManager.removeNick(member);
				event.getMessage().reply("Removed nick").queue();
			}
			else {
				event.getMessage().reply("You don't have a nick").queue();
			}
		}
		else if (nick.contains("\"") || nick.contains("\\")) {
			throw new CommandException("Illegal characters in nick");
		}
		else {
			DiscordNickManager.setNick(member, nick);
			event.getMessage().reply("Set your nick to `" + DiscordUtils.sanitizeMentions(nick) + "`").queue();
		}
	}
}
