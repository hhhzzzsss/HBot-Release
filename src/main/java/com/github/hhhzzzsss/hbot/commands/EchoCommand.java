package com.github.hhhzzzsss.hbot.commands;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.discord.DiscordUtils;

import lombok.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class EchoCommand implements ChatCommand, DiscordCommand {
	private final Bot bot;

	@Override
	public String getName() {
		return "echo";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"<text>"};
	}

	@Override
	public String getDescription() {
		return "Repeats whatever text you specify";
	}

	@Override
	public int getPermission() {
		return 0;
	}

	@Override
	public void executeChat(ChatSender sender, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		bot.sendChat(parser.readString(true));
	}
	
	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		ArgsParser parser = new ArgsParser(this, args);
		event.getChannel().sendMessage(DiscordUtils.sanitizeMentions(parser.readString(true))).queue();
	}
}
