package com.github.hhhzzzsss.hbot.subBots;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.entity.EntityTracker;
import com.github.hhhzzzsss.hbot.entity.PlayerEntity;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;

import java.util.UUID;

public class OrbitBot extends Bot {
	@Getter Bot parent;
	@Getter private String targetUsername;
	@Getter private UUID targetUUID;
	private EntityTracker entityTracker = new EntityTracker();
	
	public OrbitBot(Bot parent, String targetUsername, UUID targetUUID) {
		super(getRandomUsername(), parent.getHost(), parent.getPort());
		this.parent = parent;
		this.targetUsername = targetUsername;
		this.targetUUID = targetUUID;
		
		configureStateManager();
		configureEntityTracker();
	}
	
	private void configureStateManager() {
		stateManager.setAutoOp(true);
		stateManager.setAutoUnmute(true);
	}
	private void configureEntityTracker() {
		entityTracker.setTrackEntities(false);
		entityTracker.setTrackLiving(false);
	}
	
	private static String getRandomUsername() {
		String charList = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder sb = new StringBuilder(8); 
  
        for (int i = 0; i < 8; i++) { 
            int index = (int)(charList.length() * Math.random());
            sb.append(charList.charAt(index)); 
        }
		return "§borbit_" + sb.toString();
	}

	
	int timeout = 0;
	int maxTimeout = 20*60;
	double orbitSpeed = 0.15;
	double orbitRadius = 2.0;
	double orbitAngle = 0.0;
	
	@Override
	protected void onTick() {
		if (timeout > maxTimeout) {
			sendChatInstantly("Could not find target. Disconnecting...");
			stop();
			return;
		}
		if (!isLoggedIn()) {
			return;
		}
		
		PlayerEntity targetEntity = (PlayerEntity) entityTracker.getEntityByUuid(targetUUID);
		if (targetEntity == null) {
			timeout++;
			attemptTp();
		}
		else {
			timeout = 0;
			double dx = targetEntity.getX() - posManager.getX();
			double dy = targetEntity.getY() - posManager.getY();
			double dz = targetEntity.getZ() - posManager.getZ();
			double d2 = dx*dx + dy*dy + dz*dz;
			if (d2 > 40*40) {
				attemptTp();
			}
			else {
				double x = targetEntity.getX() + orbitRadius*Math.cos(orbitAngle);
				double y = targetEntity.getY();
				double z = targetEntity.getZ() + orbitRadius*Math.sin(orbitAngle);
				posManager.moveLook(x, y, z, targetEntity.getYaw(), targetEntity.getPitch());
				orbitAngle += orbitSpeed;
				orbitAngle %= Math.PI*2.0;
			}
		}
	}

	long nextCommand = System.currentTimeMillis();
	long commandDelay = 1000*2;
	private void attemptTp() {
		long currentTime = System.currentTimeMillis();
		if (nextCommand <= currentTime) {
			nextCommand = currentTime + commandDelay;
			sendCommand("/tp " + targetUUID);
		}
	}
	
	@Override
	protected void onPacket(Packet packet) {
		if (packet instanceof ClientboundChatPacket) {
			ClientboundChatPacket t_packet = (ClientboundChatPacket) packet;
			String text = ChatUtils.getFullText(t_packet.getMessage());
			if (text.equals("Error: Player not found.") || text.equals("Error: null.")) {
				sendChatInstantly("Could not teleport to target. Disconnecting...");
				stop();
				return;
			}
			else if (text.startsWith("Error: ") && text.endsWith(" has teleportation disabled.")) {
				sendChatInstantly("Target has teleportation disabled. Disconnecting...");
				stop();
				return;
			}
		}
	}
	
	protected void onDisconnect(DisconnectedEvent event) {
		if (!parent.isLoggedIn()) {
			stop();
		}
	}
}
