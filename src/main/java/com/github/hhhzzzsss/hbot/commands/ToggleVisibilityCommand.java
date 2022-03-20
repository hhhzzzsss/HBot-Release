package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.*;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class ToggleVisibilityCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "togglevisibility";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {};
	}

	@Override
	public String getDescription() {
		return "Toggles whether HBot can be seen";
	}

	@Override
	public int getPermission() {
		return 1;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		execute(PlatformInfo.getMinecraft(hbot));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(PlatformInfo.getDiscord(hbot, event, "Visibility"));
	}
	
	private void execute(PlatformInfo platform) throws CommandException {
		if (hbot.getStateManager().isTargetVanish()) {
			hbot.getStateManager().setTargetVanish(false);
			platform.sendMessage("&7HBot is now &3visible");
		}
		else {
			hbot.getStateManager().setTargetVanish(true);
			platform.sendMessage("&7HBot is now &3invisible");
		}
	}
}
