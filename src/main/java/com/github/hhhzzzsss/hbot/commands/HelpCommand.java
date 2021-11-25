package com.github.hhhzzzsss.hbot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.Command;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.CommandList;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.Permission;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.discord.DiscordManager;
import com.github.hhhzzzsss.hbot.modules.ChatCommandHandler;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@RequiredArgsConstructor
public class HelpCommand implements ChatCommand, DiscordCommand {
	public final Bot bot;
	public final CommandList commandList;
	public final ChatCommandHandler commandHandler;
	public DiscordManager discordManager;
	public CommandCore commandCore;
	
	public HelpCommand(Bot bot, CommandList commandList, ChatCommandHandler commandHandler, DiscordManager discordManager) {
		this(bot, commandList, commandHandler);
		this.discordManager = discordManager;
	}
	
	public HelpCommand(Bot bot, CommandList commandList, ChatCommandHandler commandHandler, DiscordManager discordManager, CommandCore commandCore) {
		this(bot, commandList, commandHandler);
		this.discordManager = discordManager;
		this.commandCore = commandCore;
	}
	
	public HelpCommand(Bot bot, CommandList commandList, ChatCommandHandler commandHandler, CommandCore commandCore) {
		this(bot, commandList, commandHandler);
		this.commandCore = commandCore;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String[] getSyntax() {
		return new String[] {"[command]"};
	}

	@Override
	public String getDescription() {
		return "Lists or explains commands";
	}

	@Override
	public int getPermission() {
		return 0;
	}
	
	@Override
	public void executeChat(String sender, String args) throws CommandException {
		String prefix = commandHandler.getPrefix();
		if (args.length() == 0) { // List commands
			StringBuilder sb = new StringBuilder();
			sb.append("&7Commands -");
			for (Command command : getSortedCommandList()) if (command instanceof ChatCommand) {
				if (command.getPermission() == 0) {
					sb.append(" &a");
				}
				else if (command.getPermission() == 1) {
					sb.append(" &c");
				}
				else {
					sb.append(" &4");
				}
				sb.append(prefix);
				sb.append(command.getName());
			}
			if (commandCore != null && sb.length() > 256) {
				commandCore.run("bcraw " + sb.toString());
			}
			else {
				bot.sendChat(sb.toString());
			}
		}
		else { // Help message for specific command
			Command command = commandList.get(args.split(" ", 2)[0].toLowerCase());
			Permission[] perms = Permission.values();
			if (command == null) {
				throw new CommandException("Unknown command: " + args);
			}
			StringBuilder sb = new StringBuilder();
			if (command.getPermission() > 0) {
				sb.append(String.format("&c[%s] ", perms[command.getPermission()].name()));
			}
			sb.append(String.format("&b%s%s ", prefix, command.getName()));
			if (command.getSyntax().length == 0) {
				sb.append(String.format("&7- %s", command.getDescription()));
			}
			else if (command.getSyntax().length == 1) {
				sb.append(String.format("&3%s &7- %s", command.getSyntax()[0], command.getDescription()));
			}
			else {
				sb.append(String.format("&7- %s", command.getDescription()));
				for (String syntax : command.getSyntax()) {
					sb.append(String.format("\n&b%s%s &3%s", prefix, command.getName(), syntax));
				}
			}
			if (commandCore != null && (sb.length() > 256 || command.getSyntax().length > 1)) {
				commandCore.run("bcraw " + sb.toString());
			}
			else {
				bot.sendChat(sb.toString());
			}
		}
	}
	


	@Override
	public void executeDiscord(MessageReceivedEvent event, String args) throws CommandException {
		if (discordManager == null) {
			throw new CommandException("In order to run discord commands, you must initialize HelpCommand with the discord manager");
		}
		
		String prefix = discordManager.getPrefix();
		Permission[] perms = Permission.values();
		if (args.length() == 0) { // List commands
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(new Color(100, 149, 237));
			eb.setTitle("Help");
			StringBuilder sb = null;
			int lastPerm = -1;
			for (Command command : getSortedCommandList()) if (command instanceof DiscordCommand) {
				if (command.getPermission() != lastPerm) {
					if (sb != null) {
						if (lastPerm != -1) {
							sb.delete(sb.length()-3, sb.length());
							sb.append('\n');
						}
						eb.addField("Commands for " + perms[lastPerm].discordRank(), sb.substring(0,sb.length()-1), false);
					}
					sb = new StringBuilder();
					lastPerm = command.getPermission();
				}
				sb.append(String.format("`%s%s` | ", prefix, command.getName(), command.getDescription()));
			}
			if (lastPerm != -1) {
				sb.delete(sb.length()-3, sb.length());
				sb.append('\n');
			}
			eb.addField("Commands for " + perms[lastPerm].discordRank(), sb.substring(0,sb.length()-1), false);
			event.getChannel().sendMessage(eb.build()).queue();
		}
		else { // Help message for specific command
			Command command = commandList.get(args.split(" ", 2)[0].toLowerCase());
			if (command == null) {
				throw new CommandException("Unknown command: " + args);
			}
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(new Color(100, 149, 237));
			eb.setTitle("Help - " + command.getName());
			eb.appendDescription(command.getDescription());
			if (command.getPermission() > 0) {
				eb.appendDescription(String.format("\n*Requires **%s** perms*", perms[command.getPermission()].discordRank()));
			}
			for (String syntax : command.getSyntax()) {
				eb.appendDescription(String.format("\n`%s%s` %s", prefix, command.getName(), syntax));
			}
			event.getChannel().sendMessage(eb.build()).queue();
		}
	}
	
	public List<Command> getSortedCommandList() {
		List<Command> list = new ArrayList<>(commandList.getCommands());
		list.sort(new Comparator<Command>() {
			@Override
			public int compare(Command c1, Command c2) {
				if (c1.getPermission() != c2.getPermission()) {
					return c1.getPermission()-c2.getPermission();
				}
				else {
					return c1.getName().compareTo(c2.getName());
				}
			}
		});
		return list;
	}
}
