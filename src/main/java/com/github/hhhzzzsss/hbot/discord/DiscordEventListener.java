package com.github.hhhzzzsss.hbot.discord;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.Logger;
import com.github.hhhzzzsss.hbot.command.Command;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.DiscordCommand;
import com.github.hhhzzzsss.hbot.command.Permission;
import com.github.hhhzzzsss.hbot.util.ChatUtils;

import lombok.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		if (event.getAuthor().isBot() || !event.getMessage().getCategory().getName().equalsIgnoreCase(hbot.getCategoryName())) {
			return;
		}
		
		Message msg = event.getMessage();
		String raw = msg.getContentDisplay();
		User user = event.getAuthor();
		Member member = event.getMember();
		
		if (event.getChannel().getName().equalsIgnoreCase("hbot-logs") && !raw.startsWith(discordManager.getPrefix())) {
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
				processCommand(event, command, args);
			}
			catch (CommandException e) {
				msg.reply(DiscordUtils.sanitizeMentions(e.getMessage())).queue();
			}
		}
	}
	
	private void processCommand(MessageReceivedEvent event, String commandName, String args) throws CommandException {
		Command command = hbot.getCommandList().get(commandName.toLowerCase());
		if (command == null) {
			throw new CommandException("Unknown command: " + commandName);
		}
		if (!(command instanceof DiscordCommand)) {
			throw new CommandException("This command cannot be run from Discord");
		}
		if (command.getPermission() > DiscordUtils.getMemberPermissionInt(event.getMember())) {
			throw new CommandException("You don't have permission to run this command");
		}
		
		((DiscordCommand) command).executeDiscord(event, args);
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
