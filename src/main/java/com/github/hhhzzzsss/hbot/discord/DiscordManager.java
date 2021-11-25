package com.github.hhhzzzsss.hbot.discord;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.command.Permission;
import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

@RequiredArgsConstructor
public class DiscordManager implements PacketListener, DisconnectListener {
	public final HBot hbot;
	@Getter @Setter @NonNull private String prefix;
	public JDA jda;
	public DiscordEventListener listener;
	public boolean connected = false;
	public boolean finished = false;
	
	public ArrayList<TextChannel> logChannels = new ArrayList<>();
	
	public void login() {
		JDABuilder builder = JDABuilder.createDefault(hbot.getBotToken());
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		builder.setActivity(Activity.playing("Loading..."));
		listener = new DiscordEventListener(this, hbot);
		builder.addEventListeners(listener);
		try {
			jda = builder.build();
		} catch (LoginException e) {
			System.err.println("Failed to connect to discord");
			e.printStackTrace();
			finished = true;
		}
		hbot.getExecutor().scheduleAtFixedRate(() -> {
			try {
				onDiscordTick();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}, 50, 50, TimeUnit.MILLISECONDS);
	}
	
	public void setConnectedStatus(boolean connected) {
		if (connected) {
			setPlaying("Connected! Try !help for a list of commands.");
		}
		else {
			setPlaying("Connecting...");
		}
	}
	
	public void setPlaying(String name) {
		if (!jda.getPresence().getActivity().getName().equals(name)) jda.getPresence().setActivity(Activity.playing(name));
	}
	
	public void loadLogChannels() {
		ArrayList<TextChannel> channels = new ArrayList<>();
		for (Guild guild : jda.getGuilds()) {
			for (Category category : guild.getCategoriesByName(hbot.getCategoryName(), true)) {
				System.out.println(hbot.getCategoryName());
				for (TextChannel channel : category.getTextChannels()) {
					if (channel.getName().equalsIgnoreCase("hbot-logs")) {
						channels.add(channel);
					}
				}
			}
		}
		logChannels = channels;
	}
	
	public void shutdown() {
		sendInLogChannels("Stopping...");
		jda.shutdown();
	}
	
	public void shutdownNow() {
		jda.shutdownNow();
	}

	@Override
	public void onDisconnected(DisconnectedEvent packet) {
		setConnectedStatus(false);
	}

	private StringBuilder logMessage = new StringBuilder();
	@Getter @Setter private long logDelay = 1000;
	private long nextLogTime = System.currentTimeMillis();
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ServerJoinGamePacket) {
			setConnectedStatus(true);
		}
	}
	
	public void queueLogMessage(String message) {
		message = DiscordUtils.sanitizeMentions(message);
		synchronized (logMessage) {
			if (logMessage.length() < 2000) {
				if (logMessage.length() > 0) {
					logMessage.append('\n');
				}
				logMessage.append(message);
			}
		}
	}

	boolean doneSendingInLogs = true;
	public void sendInLogChannels(String message) {
		for (TextChannel channel : logChannels) {
			doneSendingInLogs = false;
			channel.sendMessage(message).queue(
					(msg) -> doneSendingInLogs = true,
					(err) -> doneSendingInLogs = true);
		}
	}
	
	public void onDiscordTick() {
		synchronized (logMessage) {
			if (logMessage.length() == 0 || logChannels.size() == 0) {
				return;
			}
		}
		
		long currentTime = System.currentTimeMillis();
		if ((currentTime >= nextLogTime && doneSendingInLogs) || currentTime - nextLogTime > 5000) {
			nextLogTime = currentTime + logDelay;
			String message;
			synchronized (logMessage) {
				message = logMessage.toString();
				if (message.length() >= 2000) {
					message = message.substring(0, 2000);
				}
				logMessage.setLength(0);
			}
			sendInLogChannels(message);
		}
	}
}
