package com.github.hhhzzzsss.hbot.modules;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.Config;
import com.github.hhhzzzsss.hbot.command.ChatCommand;
import com.github.hhhzzzsss.hbot.command.Command;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.CommandList;
import com.github.hhhzzzsss.hbot.command.Permission;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.hhhzzzsss.hbot.util.HashUtils;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;
import net.kyori.adventure.text.Component;

public class ChatCommandHandler implements PacketListener {
	private final Bot bot;
	private final CommandList commandList;
	private final PlayerListTracker playerListTracker;
	@Getter private String prefix;
	@Getter private Pattern essentialsPattern;
	@Getter private Pattern vanillaPattern;
	@Getter @Setter private String ownerKey = Config.getConfig().getOwnerKey();
	@Getter @Setter private String adminKey = Config.getConfig().getAdminKey();
	@Getter @Setter private String trustedKey = Config.getConfig().getTrustedKey();
	
	public ChatCommandHandler(Bot bot, CommandList commandList, PlayerListTracker playerListTracker, String prefix) {
		this.bot = bot;
		this.commandList = commandList;
		this.playerListTracker = playerListTracker;
		this.setPrefix(prefix);
	}
	
	public void onPacket(Packet packet) {
		if (packet instanceof ServerChatPacket) {
			ServerChatPacket t_packet = (ServerChatPacket) packet;
			Component message = t_packet.getMessage();
			String strMessage = ChatUtils.getFullText(message);
			
			if (t_packet.getSenderUuid().equals(new UUID(0, 0)) || t_packet.getSenderUuid().equals(bot.getUuid())) {
				return;
			}
			
			Matcher m;
			String username;
			String command;
			String args;
			if ((m = essentialsPattern.matcher(strMessage)).matches()) {
				username = m.group(1);
				command = m.group(2);
				args = m.group(3).trim();
			}
			else if ((m = vanillaPattern.matcher(strMessage)).matches()) {
				username = m.group(1);
				command = m.group(2);
				args = m.group(3).trim();
			}
			else {
				return;
			}
			
			String recordedName = playerListTracker.getRecordedLoginName(t_packet.getSenderUuid());
			if (recordedName != null) {
				username = recordedName;
			}
			
			try {
				runCommand(username, command, args);
			}
			catch (CommandException e) {
				bot.sendChat("&c" + e.getMessage());
			}
		}
	}
	
	public void runCommand(String sender, String commandName, String args) throws CommandException {
		Command command = commandList.get(commandName.toLowerCase());
		if (command == null) {
			throw new CommandException("Unknown command: " + commandName);
		}
		if (!(command instanceof ChatCommand)) {
			throw new CommandException("This command cannot be run from Minecraft chat");
		}
		
		if (command.getPermission() > 0) {
			if (args.length() == 0) {
				throw new CommandException("This command requires a hash for verification");
			}
			String unformattedSender = sender.replaceAll("§[0-9a-fklmnor]", "");
			int splitIdx = args.lastIndexOf(" ");
			String hash = args.substring(splitIdx+1);
			args = args.substring(0, Math.max(splitIdx, 0));
			int permLevel = 0;
			String plainTextBase = prefix + commandName + (args.isEmpty() ? "" : " " + args) + ";" + unformattedSender;
			if (HashUtils.isValidHash(plainTextBase, hash, ownerKey)) {
				permLevel = Permission.OWNER.asInt();
			}
			else if (HashUtils.isValidHash(plainTextBase, hash, adminKey)) {
				permLevel = Permission.ADMIN.asInt();
			}
			else if (HashUtils.isValidHash(plainTextBase, hash, trustedKey)) {
				permLevel = Permission.TRUSTED.asInt();
			}
			else {
				throw new CommandException("Invalid hash");
			}
			
			if (permLevel < command.getPermission()) {
				throw new CommandException("You don't have permission to run this command");
			}
		}
		
		((ChatCommand) command).executeChat(sender, args);
	}
	
	/**
	 * Sets the prefix and compiles the a new command pattern using this prefix
	 * 
	 * @param prefix The command prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		essentialsPattern = Pattern.compile(String.format("(?:.* )?(\\S*): %s(\\S+)(.*)?", prefix));
		vanillaPattern = Pattern.compile(String.format("<(\\S*)> %s(\\S+)(.*)?", prefix));
	}
}
