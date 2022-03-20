package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@RequiredArgsConstructor
public class MiscChatReactionsHandler implements PacketListener {

	private final HBot hbot;
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundChatPacket) {
			ClientboundChatPacket t_packet = (ClientboundChatPacket) packet;
			Component message = t_packet.getMessage();
			String strMessage = ChatUtils.getFullText(message);
			if (!t_packet.getSenderUuid().equals(new UUID(0, 0)) && !t_packet.getSenderUuid().equals(hbot.getUuid())) {
				/*if (strMessage.toLowerCase().matches(".*\\bamogus\\b.*")) {
					if (hbot.getMusicPlayer().noActiveSong()) {
						try {
							hbot.getMusicPlayer().play("amogus");
						} catch (IOException | InvalidMidiDataException e) {
							e.printStackTrace();
						}
					}
				}*/
			}
		}
		
	}
	
}
