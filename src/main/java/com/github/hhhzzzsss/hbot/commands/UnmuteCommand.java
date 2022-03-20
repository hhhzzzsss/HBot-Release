package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.GlobalDiscordCommand;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.discord.MuteManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

@RequiredArgsConstructor
public class UnmuteCommand implements GlobalDiscordCommand {
	@Override
	public String getName() {
		return "unmute";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"list",
			"<discord tag>",
		};
	}

	@Override
	public String getDescription() {
		return "Unmutes discord members";
	}

	@Override
	public int getPermission() {
		return 1;
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		PlatformInfo platform = PlatformInfo.getDiscord(null, event, "Mute");
		if (args.length() == 0 || args.equalsIgnoreCase("list")) {
			MuteManager.readMuteList();
			if (MuteManager.getMutelist().size() == 0) {
				platform.sendDiscordOnlyMessage("There are currently no muted members");
			}
			else {
				platform.sendDiscordOnlyMessage("Muted members - `" + String.join("` | `", MuteManager.getMutelist()) + "`");
			}
		} else {
			Member member;
			List<Member> mentions = event.getMessage().getMentionedMembers();
			if (mentions.size() == 0) {
				try {
					member = event.getGuild().getMemberByTag(args);
				} catch (Exception e) {
					throw new CommandException(e.getMessage());
				}
				if (member == null) throw new CommandException("Member " + args + " could not be found");
			} else if (mentions.size() == 1) {
				member = mentions.get(0);
			} else {
				throw new CommandException("You can only mute one member at a time");
			}
			MuteManager.unmute(member);
			platform.sendDiscordOnlyMessage("Unmuted " + member.getUser().getAsTag());
		}
	}
}
