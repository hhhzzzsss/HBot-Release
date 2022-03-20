package com.github.hhhzzzsss.hbot.discord;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.Logger;
import com.github.hhhzzzsss.hbot.command.*;
import com.github.hhhzzzsss.hbot.util.ChatUtils;

import lombok.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class DiscordEventListener extends ListenerAdapter {
	
	private final DiscordManager discordManager;
	private final HBot hbot;
	
	@Override
	public void onReady(ReadyEvent event) {
		discordManager.connected = true;
		discordManager.setConnectedStatus(hbot.isLoggedIn());
		discordManager.loadLogChannels();
		System.out.println("Discord is ready!");
	}
	
	public static final String bridgeMessageFormat = "/tellraw @a [{\"text\":\"\"},{\"text\":\"§8[§9HBot Discord§8] \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"§9Click to join the Discord\"}]},\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.gg/5wv6xQEj9X\"}},{\"text\":\"§3%s \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"§b%s\\n§7%s\\n\\n§9Click to copy Discord tag\"}]},\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"%s\"}},{\"text\":\"§8› §7%s\"}]";
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}

		if (hbot.isDefault()) {
			MuteManager.checkMember(event.getMember());
		}

		Message message = event.getMessage();

		boolean hasNoCategory = true;
		for (String category : HBot.getCategoryList()) {
			if (message.getCategory().getName().equalsIgnoreCase(category)) {
				hasNoCategory = false;
			}
		}
		boolean inOwnCategory = message.getCategory().getName().equalsIgnoreCase(hbot.getCategoryName());

		if (!inOwnCategory) {
			if (!hbot.isDefault()) {
				return;
			}
			else if (!hasNoCategory) {
				return;
			}
		}

		String raw = message.getContentDisplay();
		User user = event.getAuthor();
		Member member = event.getMember();
		
		if (event.getChannel().getName().equalsIgnoreCase("hbot-logs") && inOwnCategory && !raw.startsWith(discordManager.getPrefix())) {
			//hbot.sendCommand(String.format("/bcraw &8[&9HBot Discord&8] &3%s &8› &7%s", event.getMember().getEffectiveName(), raw));
			String nick = DiscordNickManager.getNick(member).replaceAll("&([0-9a-frlonmk])", "§$1");
			String tag = user.getAsTag();
			String rank = DiscordUtils.getMemberPermission(member).name().replace(Permission.NONE.name(), "NO RANK");
			hbot.getCommandCore().run(String.format(bridgeMessageFormat, nick, tag, rank, tag, ChatUtils.escapeString(raw)));
			Logger.log(String.format("[Discord] %s -> %s",  event.getAuthor().getName(), raw));
		}
		
		String prefix = discordManager.getPrefix();
		if (raw.startsWith(prefix)) {
			String[] split = raw.substring(prefix.length()).split(" ", 2);
			String command = split[0];
			String args = (split.length == 2 ? split[1] : "").trim();
			try {
				processCommand(event, command, args, hasNoCategory);
			}
			catch (CommandException e) {
				message.reply(DiscordUtils.sanitizeMentions(e.getMessage())).queue();
			}
		}
	}
	
	private void processCommand(MessageReceivedEvent event, String commandName, String args, boolean calledOutOfCateogory) throws CommandException {
		Command command = hbot.getCommandList().get(commandName.toLowerCase());
		if (command == null) {
			//throw new CommandException("Unknown command: " + commandName);
			return;
		}
		if (!(command instanceof DiscordCommand)) {
			throw new CommandException("This command cannot be run from Discord");
		}
		if (calledOutOfCateogory && !(command instanceof GlobalDiscordCommand)) {
			throw new CommandException(
					"This command can only be run inside a channel category corresponding to one of the servers ("
					+String.join(", ", HBot.getCategoryList())
					+")");
		}
		if (command.getPermission() > DiscordUtils.getMemberPermissionInt(event.getMember())) {
			throw new CommandException("You don't have permission to run this command");
		}
		
		((DiscordCommand) command).executeDiscord(event, args);
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (hbot.isDefault()) {
			MuteManager.checkMember(event.getMember());
		}
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) {
		discordManager.connected = false;
		discordManager.setPlaying("Reloading...");
	}
	
	@Override
	public void onReconnected(ReconnectedEvent event) {
		discordManager.connected = true;
		discordManager.setConnectedStatus(hbot.isLoggedIn());
	}
	
	@Override
	public void onResumed(ResumedEvent event) {
		discordManager.connected = true;
		discordManager.setConnectedStatus(hbot.isLoggedIn());
	}
	
	@Override 
	public void onShutdown(ShutdownEvent event) {
		discordManager.finished = true;
	}
}
