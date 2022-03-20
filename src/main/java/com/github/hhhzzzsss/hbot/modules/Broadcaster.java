package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.listeners.TickListener;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Broadcaster implements PacketListener, TickListener {
	private final Bot bot;
	@Getter @Setter private String loginMessage;
	@Getter @Setter private long broadcastInterval = 3 * 60 * 1000;
	private long nextBroadcastTime = System.currentTimeMillis();
	private int broadcastIndex = 0;
	private ArrayList<String> broadcasts = new ArrayList<>();
	
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundLoginPacket && loginMessage != null) {
			bot.sendChat(loginMessage);
		}
	}
	
	public void onTick() {
		if (broadcasts.size() == 0) {
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime >= nextBroadcastTime) {
			if (broadcastIndex >= broadcasts.size()) {
				broadcastIndex = 0;
			}
			bot.sendChat(broadcasts.get(broadcastIndex));
			broadcastIndex++;
			nextBroadcastTime = currentTime + broadcastInterval;
		}
	}
	
	public void addBroadcast(String message) {
		broadcasts.add(message);
	}
	public void removeBroadcast(int index) {
		broadcasts.remove(index);
	}
	public void removeBroadcast(String message) {
		broadcasts.remove(message);
	}
	public void clearBroadcasts() {
		broadcasts.clear();
	}
	public String getBroadcast(int index) {
		return broadcasts.get(index);
	}
	public void setBroadcast(int index, String str) {
		broadcasts.set(index, str);
	}
	public int numBroadcasts() {
		return broadcasts.size();
	}
	
	public void setInitialBroadcastDelay(long delay) {
		nextBroadcastTime = System.currentTimeMillis() + delay;
	}
	
	public ScheduledFuture<?> scheduleBroadcast(String message, int period, TimeUnit unit) {
		return bot.getExecutor().scheduleAtFixedRate(() -> {
			if (bot.isLoggedIn()) {
				bot.sendChat(message);
			}
		}, period, period, unit);
	}
}
