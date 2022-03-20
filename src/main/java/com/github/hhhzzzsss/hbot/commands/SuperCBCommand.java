package com.github.hhhzzzsss.hbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.entity.PlayerData;
import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class SuperCBCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "supercb";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {
			"<command>",
			"...%tlist... - Runs for every username",
			"...%ulist... - Runs for every uuid",
			"...%t{regex}... - Runs for every player matching that regex",
			"...%u{regex}... - Same thing but for uuid"
		};
	}

	@Override
	public String getDescription() {
		return "Extended cb command";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String command = parser.readString(true);
		execute(command);
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String command = parser.readString(true);
		execute(command);
	}

	private static Pattern tPattern = Pattern.compile("%t\\{(.*?)\\}");
    private static Pattern uPattern = Pattern.compile("%u\\{(.*?)\\}");
	private void execute(String command) throws CommandException {
		Matcher tMatcher = tPattern.matcher(command);
		Matcher uMatcher = uPattern.matcher(command);
		if (tMatcher.find()) {
			Pattern p = Pattern.compile(tMatcher.group(1));
			for (PlayerData data : hbot.getPlayerListTracker().getPlayerList().values()) if (p.matcher(data.getName()).matches()) {
				String replacedCommand = new StringBuilder(command).replace(tMatcher.start(), tMatcher.end(), data.getName()).toString();
				replacedCommand = replacedCommand.replace("%tlist", data.getName()).replace("%ulist", data.getUUID().toString());
				if (!replacedCommand.contains("%tlist") && !replacedCommand.contains("%ulist") && !tPattern.matcher(data.getName()).find() && !uPattern.matcher(data.getName()).find()) {
					execute(replacedCommand);
				}
			}
		}
		else if (uMatcher.find()) {
			Pattern p = Pattern.compile(uMatcher.group(1));
			for (PlayerData data : hbot.getPlayerListTracker().getPlayerList().values()) if (p.matcher(data.getName()).matches()) {
				String replacedCommand = new StringBuilder(command).replace(uMatcher.start(), uMatcher.end(), data.getUUID().toString()).toString();
				replacedCommand = replacedCommand.replace("%tlist", data.getName()).replace("%ulist", data.getUUID().toString());
				if (!replacedCommand.contains("%tlist") && !replacedCommand.contains("%ulist") && !tPattern.matcher(data.getName()).find() && !uPattern.matcher(data.getName()).find()) {
					execute(replacedCommand);
				}
			}
		}
		else if (command.contains("%tlist") || command.contains("%ulist")) {
			for (PlayerData data : hbot.getPlayerListTracker().getPlayerList().values()) {
				String replacedCommand = command.replace("%tlist", data.getName()).replace("%ulist", data.getUUID().toString());
				if (!replacedCommand.contains("%tlist") && !replacedCommand.contains("%ulist") && !tPattern.matcher(data.getName()).find() && !uPattern.matcher(data.getName()).find()) {
					execute(replacedCommand);
				}
			}
		}
		else {
			hbot.getCommandCore().run(command);
		}
	}
}
