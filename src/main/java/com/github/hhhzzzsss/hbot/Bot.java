package com.github.hhhzzzsss.hbot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.hhhzzzsss.hbot.listeners.*;
import com.github.hhhzzzsss.hbot.modules.*;
import com.github.hhhzzzsss.hbot.util.HashUtils;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

import com.github.steveice10.packetlib.tcp.TcpClientSession;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.*;

public abstract class Bot {
	public static final ArrayList<Bot> BOTLIST = new ArrayList<>();
	
	@Getter private String username;
	@Getter @Setter private String nextUsername;
	@Getter private UUID uuid;
	@Getter private final String host;
	@Getter private final int port;
	@Getter @Setter private ProxyInfo PROXY = null;
	@Getter private Session session = null;
	@Getter private boolean running = true;
	@Getter private boolean loggedIn = false;
	@Getter @Setter private boolean autoRelog = true;
	
	@Getter private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private ArrayList<PacketListener> packetListeners = new ArrayList<>();
	private ArrayList<TickListener> tickListeners = new ArrayList<>();
	private ArrayList<DisconnectListener> disconnectListeners = new ArrayList<>();
	private Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
	
	@Getter protected ChatQueue commandQueue = new ChatQueue(this);
	@Getter protected StateManager stateManager = new StateManager(this);
	@Getter protected PositionManager posManager = new PositionManager(this);
	
	public Bot(String username, String host, int port) {
		this.nextUsername = username;
		this.username = this.nextUsername; // this might be redundant but I'll keep it just in case
		this.uuid = HashUtils.getOfflineUUID(username); // this might be redundant but I'll keep it just in case
		this.host = host;
		this.port = port;
		synchronized(BOTLIST) {
			BOTLIST.add(this);
		}
	}
	
	public void start() {
		getListeners();
		scheduleTicking();
		connect();
	}
	
	private void connect() {
		username = nextUsername;
		uuid = HashUtils.getOfflineUUID(username);
		MinecraftProtocol protocol = new MinecraftProtocol(username);
		session = new TcpClientSession(host, port, protocol, PROXY);
		session.addListener(new SessionAdapter() {
			@Override
            public synchronized void packetReceived(PacketReceivedEvent event) {
				packetQueue.add(event.getPacket());
			}
			
			@Override
		    public void disconnected(DisconnectedEvent event) {
				FastThreadLocal.removeAll();
				executor.submit(() -> {
					FastThreadLocal.removeAll();
					processDisconnect(event);
				});
		    }
		});
		session.connect();
	}

	// loop through the fields of the class and all of its superclasses up until the Bot class
	private void getListeners() {
		Class<?> c = this.getClass();
		do {
			for (Field field : c.getDeclaredFields()) {
				field.setAccessible(true);
				Object value;
				try {
					value = field.get(this);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					System.err.println("Error when accesing field: " + field.getName());
					e.printStackTrace();
					continue;
				}
				field.setAccessible(false);
				
				if (value instanceof PacketListener) {
					packetListeners.add((PacketListener) value);
				}
				if (value instanceof TickListener) {
					tickListeners.add((TickListener) value);
				}
				if (value instanceof DisconnectListener) {
					disconnectListeners.add((DisconnectListener) value);
				}
			}
			c = c.getSuperclass();
		} while (Bot.class.isAssignableFrom(c));
	}
	
	private void scheduleTicking() {
		executor.scheduleAtFixedRate(() -> {
			try {
				processTick();
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}, 0, 50, TimeUnit.MILLISECONDS);
	}

	// Packets are processed at the beginning of ticks
	private void processTick() {
		int queueLength = packetQueue.size();
		for (int i=0; i<queueLength; i++) {
			processPacket(packetQueue.poll());
		}
		
		if (loggedIn) {
			try {
				onTick();
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
			
			for (TickListener listener : tickListeners) {
				try {
					listener.onTick();
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public interface PacketFuture {
		public boolean onPacket(Packet p);
	}
	
	@AllArgsConstructor
	private class ExpirablePacketFuture implements PacketFuture {
		private PacketFuture future;
		private long expiration;
		@Override
		public boolean onPacket(Packet packet) {
			if (System.currentTimeMillis() < expiration) {
				return future.onPacket(packet);
			}
			else {
				return false;
			}
		}
	}
	
	public List<PacketFuture> packetFutures = new LinkedList<>();
	private void processPacket(Packet packet) {
		if (packet instanceof ServerJoinGamePacket) {
			loggedIn = true;
		}
		
		try {
			onPacket(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		for (PacketListener listener : packetListeners) {
			try {
				listener.onPacket(packet);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		synchronized (packetFutures) {
			Iterator<PacketFuture> itr = packetFutures.iterator();
			while (itr.hasNext()) {
				PacketFuture future = itr.next();
				if (future.onPacket(packet)) {
					itr.remove();
				}
			}
		}
	}
	
	private void processDisconnect(DisconnectedEvent event) {
		loggedIn = false;
		//System.out.println("Disconnected: " + event.getReason());
		
		try {
			onDisconnect(event);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		for (DisconnectListener listener : disconnectListeners) {
			try {
				listener.onDisconnected(event);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (autoRelog) {
			if (event.getReason().contains("Wait 5 seconds before connecting, thanks! :)") || event.getReason().contains("Connection throttled! Please wait before reconnecting.")) {
				executor.schedule(() -> {
					connect();
				}, 5, TimeUnit.SECONDS);
			}
			else {
				executor.schedule(() -> {
					connect();
				}, 1, TimeUnit.SECONDS);
			}
		}
		else {
			stop();
		}
	}
	
	/**
	 * A thread-safe way to schedule an task that waits for a certain packet event before running.
	 * 
	 * @param future The packet to schedule
	 */
	public void schedulePacketFuture(PacketFuture future) {
		synchronized(packetFutures) {
			packetFutures.add(future);
		}
	}
	
	/**
	 * A thread-safe way to schedule an task that waits for a certain packet event before running.
	 * 
	 * @param future The packet to schedule
	 */
	public void schedulePacketFuture(PacketFuture future, long timeout) {
		synchronized(packetFutures) {
			packetFutures.add(new ExpirablePacketFuture(future, timeout));
		}
	}
	
	public void sendPacket(Packet packet) {
		session.send(packet);
	}
	
	public void sendChat(String chat) {
		commandQueue.sendChat(chat);
	}
	
	public void sendChatAsync(String chat) {
		executor.submit(() -> {
			sendChat(chat);
		});
	}
	
	public void sendCommand(String command) {
		commandQueue.sendCommand(command);
	}
	
	public void sendChatInstantly(String chat) {
		sendPacket(new ClientChatPacket(chat));
	}
	
	public void stop() {
		running = false;
		autoRelog = false;
		try {
			onStop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			session.disconnect("bye");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		BOTLIST.remove(this);
	}
	
	public void relog() {
		try {
			session.disconnect("bye");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}
	
	public String toString() {
		return String.format("%s (%s|%s:%d)", this.getClass().getName(), username, host, port);
	}
	
	protected void onPacket(Packet packet) {}
	protected void onDisconnect(DisconnectedEvent event) {}
	protected void onTick() {}
	protected void onStop() {}
	
	public static void stopAllBotsAndAwaitTermination() {
		ArrayList<Bot> copiedList;
		synchronized(BOTLIST) {
			copiedList = new ArrayList<>(BOTLIST);
		}
		copiedList.forEach((bot) -> bot.stop());
		copiedList.forEach((bot) -> {
			try {
				bot.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
}
