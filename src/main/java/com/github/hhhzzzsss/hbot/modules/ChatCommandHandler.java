package com.github.hhhzzzsss.hbot.modules;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.Config;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.hhhzzzsss.hbot.util.HashUtils;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
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
		if (packet instanceof ClientboundChatPacket) {
			ClientboundChatPacket t_packet = (ClientboundChatPacket) packet;
			Component message = t_packet.getMessage();
			String strMessage = ChatUtils.getFullText(message);
			UUID uuid = t_packet.getSenderUuid();
			
			if (uuid.equals(new UUID(0, 0)) || uuid.equals(bot.getUuid())) {
				return;
			}
			
			Matcher m;
			String displayUsername;
			String command;
			String args;
			if ((m = essentialsPattern.matcher(strMessage)).matches()) {
				displayUsername = m.group(1);
				command = m.group(2);
				args = m.group(3).trim();
			}
			else if ((m = vanillaPattern.matcher(strMessage)).matches()) {
				displayUsername = m.group(1);
				command = m.group(2);
				args = m.group(3).trim();
			}
			else {
				return;
			}
			
			String actualName = playerListTracker.getRecordedLoginName(uuid);
			if (actualName == null) {
				actualName = displayUsername;
			}
			
			try {
				runCommand(new ChatSender(uuid, actualName, displayUsername), command, args);
			}
			catch (CommandException e) {
				bot.sendChat("&c" + e.getMessage());
			}
		}
	}
	
	public void runCommand(ChatSender sender, String commandName, String args) throws CommandException {
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
			String unformattedSender = sender.getName().replaceAll("§[0-9a-fklmnor]", "");
			int splitIdx = args.lastIndexOf(" ");
			String hash = args.substring(splitIdx+1);
			args = args.substring(0, Math.max(splitIdx, 0));
			int permLevel = 0;
			String argRaw = args.isEmpty() ? "" : " " + args;
			String nameBase = prefix + commandName + argRaw + ";" + unformattedSender;
			String uuidBase = prefix + commandName + argRaw + ";" + sender.getUuid().toString();
			if (HashUtils.isValidHash(nameBase, hash, ownerKey) || HashUtils.isValidHash(uuidBase, hash, ownerKey)) {
				permLevel = Permission.OWNER.asInt();
			}
			else if (HashUtils.isValidHash(nameBase, hash, adminKey) || HashUtils.isValidHash(uuidBase, hash, adminKey)) {
				permLevel = Permission.ADMIN.asInt();
			}
			else if (HashUtils.isValidHash(nameBase, hash, trustedKey) || HashUtils.isValidHash(uuidBase, hash, trustedKey)) {
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
