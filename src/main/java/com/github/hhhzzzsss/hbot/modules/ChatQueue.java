package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.listeners.TickListener;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ChatQueue implements TickListener {

	public final Bot bot;
	
	@Getter @Setter public long chatDelay = 100;
	@Getter @Setter public long commandDelay = 200;

	@Getter public long nextChatTime = System.currentTimeMillis();
	@Getter public long nextCommandTime = System.currentTimeMillis();
	
	@Getter @Setter public int maxChatQueue = 50;
	@Getter @Setter public int maxCommandQueue = 20;
	
	public Queue<String> chatQueue = new LinkedBlockingQueue<>();
	public Queue<String> commandQueue = new LinkedBlockingQueue<>();
	
	public void onTick() {
		long currentTime = System.currentTimeMillis();
		if (currentTime >= nextChatTime) {
			if (commandQueue.size() > 0 && currentTime >= nextCommandTime) {
				bot.sendPacket(new ServerboundChatPacket(commandQueue.poll()));
				nextChatTime = currentTime + chatDelay;
				nextCommandTime = currentTime + commandDelay;
			}
			else if (chatQueue.size() > 0) {
				bot.sendPacket(new ServerboundChatPacket(chatQueue.poll()));
				nextChatTime = currentTime + chatDelay;
			}
		}
	}
	
	public static final Pattern chatSplitter = Pattern.compile("\\G\\s*([^\r\n]{1,256}(?=\\s|$)|[^\r\n]{256})");
	public void sendChat(String chat) {
		if (chatQueue.size() < maxChatQueue) {
			chat = chat.trim().replace("§", "&");
			Matcher m = chatSplitter.matcher(chat);
			while (m.find()) {
				chatQueue.add(m.group(1));
			}
		}
	}
	
	public void sendCommand(String command) {
		command = command.replace("§", "&");
		if (command.length() <= 256 && commandQueue.size() < maxCommandQueue) {
			commandQueue.add(command);
		}
	}
}
