package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.Logger;
import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;

public class ChatLogger implements PacketListener, DisconnectListener {
	private final HBot hbot;
	@Getter @Setter private boolean writeToFile;
	
	public ChatLogger(HBot hbot, boolean writeToFile) {
		this.hbot = hbot;
		this.writeToFile = writeToFile;
	}

	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ServerJoinGamePacket) {
			log(String.format("Successfully logged in to %s:%d", hbot.getHost(), hbot.getPort()));
		}
		else if (packet instanceof ServerChatPacket) {
			ServerChatPacket t_packet = (ServerChatPacket) packet;
			String fullText = ChatUtils.getFullText(t_packet.getMessage());
			if (fullText.equals("") || fullText.startsWith("Command set: ") || fullText.matches("[\u2800-\u28FF\\s]+") || fullText.matches("[⬛\\s]{60,}")) {
				return;
			}
			log(fullText);
		}
	}

	@Override
	public void onDisconnected(DisconnectedEvent packet) {
		log("Disconnected: " + packet.getReason());
		//if (packet.getCause() != null) packet.getCause().printStackTrace();
	}
	
	public void log(String fullText) {
		String printedText = String.format("[%s] %s", hbot.getServerNick(), fullText);
		String loggedText = String.format("[%s:%d] %s", hbot.getHost(), hbot.getPort(), fullText);
		String discordText = fullText;
		System.out.println(printedText);
		if (writeToFile) Logger.log(loggedText);
		hbot.getDiscordManager().queueLogMessage(discordText);
	}
}
