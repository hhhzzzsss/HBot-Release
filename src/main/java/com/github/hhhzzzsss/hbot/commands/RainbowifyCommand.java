package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.ArgsParser;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class RainbowifyCommand implements ChatCommand, DiscordCommand {
	private final HBot hbot;

	@Override
	public String getName() {
		return "rainbowify";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<message>"};
	}

	@Override
	public String getDescription() {
		return "Turns message rainbow colored";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(String sender, String args) throws CommandException {
		execute(args);
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		execute(args);
	}
	
	private void execute(String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		String message = parser.readString(true);
		StringBuilder sb = new StringBuilder();
		float hue = 0.0f;
		for (char c : message.toCharArray()) {
			Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
			String hexString = Integer.toHexString(color.getRGB()).substring(2);
			String charInsert = (c=='\"' || c=='\\' ? "\\" : "") + c;
			sb.append(String.format("{\"text\":\"%s\",\"color\":\"#%s\"},", charInsert, hexString));
			hue += 1.0/Math.max(message.length(), 20);
		}
		sb.deleteCharAt(sb.length()-1);
		hbot.getCommandCore().run(String.format("tellraw @a [%s]", sb));
	}
}
