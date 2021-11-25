package com.github.hhhzzzsss.hbot.command;

import java.awt.Color;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.discord.DiscordUtils;

import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlatformInfo {
	public static enum Platform {
		MINECRAFT,
		DISCORD,
	}
	@Getter private final Platform platform;
	@Getter private Bot bot;
	@Getter private CommandCore core;
	@Getter private MessageReceivedEvent event;
	@Getter private Command command;
	@Getter @Setter private String title;
	
	private PlatformInfo(Platform platform) {
		this.platform = platform;
	}
	
	public static PlatformInfo getMinecraft(Bot bot) {
		PlatformInfo platformInfo = new PlatformInfo(Platform.MINECRAFT);
		platformInfo.bot = bot;
		return platformInfo;
	}
	
	public static PlatformInfo getMinecraft(Bot bot, CommandCore core) {
		PlatformInfo platformInfo = new PlatformInfo(Platform.MINECRAFT);
		platformInfo.bot = bot;
		platformInfo.core = core;
		return platformInfo;
	}
	
	public static PlatformInfo getDiscord(Bot bot, MessageReceivedEvent event) {
		PlatformInfo platformInfo = new PlatformInfo(Platform.DISCORD);
		platformInfo.bot = bot;
		platformInfo.event = event;
		return platformInfo;
	}
	
	public static PlatformInfo getDiscord(Bot bot, MessageReceivedEvent event, String title) {
		PlatformInfo platformInfo = new PlatformInfo(Platform.DISCORD);
		platformInfo.bot = bot;
		platformInfo.event = event;
		platformInfo.title = title;
		return platformInfo;
	}
	
	public void sendMessage(String message) {
		if (platform == Platform.MINECRAFT) {
			sendMinecraftMessage(message);
		}
		else if (platform == Platform.DISCORD) {
			sendMinecraftMessage(message);
			sendDiscordMessage(message);
		}
	}
	
	public void sendResponseOnlyMessage(String message) {
		if (platform == Platform.MINECRAFT) {
			sendMinecraftMessage(message);
		}
		else if (platform == Platform.DISCORD) {
			sendDiscordMessage(message);
		}
	}
	
	public void sendMinecraftOnlyMessage(String message) {
		sendMinecraftMessage(message);
	}
	
	public void sendDiscordOnlyMessage(String message) {
		if (platform == Platform.DISCORD) {
			sendDiscordMessage(message);
		}
	}
	
	private void sendMinecraftMessage(String message) {
		if (core != null && message.length() > 256 || message.contains("\n")) {
			core.run("bcraw " + message);
		}
		else {
			bot.sendChat(message);
		}
	}
	
	private void sendDiscordMessage(String message) {
		String discordMessage = message.replaceAll("&[39b](\\s*)(.+?)(\\s*)(?=&[0-9a-frlonmk]|$)", "$1`$2`$3").replaceAll("&[0-9a-frlonmk]","");
		if (title == null) {
			event.getChannel().sendMessage(DiscordUtils.sanitizeMentions(discordMessage)).queue();
		}
		else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(new Color(100, 149, 237));
			eb.setTitle(title);
			eb.setDescription(discordMessage);
			event.getChannel().sendMessage(eb.build()).queue();
		}
	}
}
