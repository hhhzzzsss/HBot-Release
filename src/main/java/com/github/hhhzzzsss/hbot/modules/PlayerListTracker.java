package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.entity.PlayerData;
import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.HashUtils;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerInfoPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListTracker implements PacketListener, DisconnectListener {
	
	
	@Getter private final HashMap<UUID, PlayerData> playerList = new HashMap<>();
	
	@Getter private final boolean mappingOfflineUUIDs;
	@Getter private HashMap<String, UUID> uuidMap = null;
	@Getter private HashMap<String, UUID> unformattedUuidMap = null;
	@Getter private HashMap<UUID, String> loginNameMap = null;
	
	public PlayerListTracker(boolean mappingOffline) {
		mappingOfflineUUIDs = mappingOffline;
		if (mappingOffline) {
			uuidMap = new HashMap<>();
			unformattedUuidMap = new HashMap<>();
			loginNameMap = new HashMap<>();
		}
	}

	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundPlayerInfoPacket) {
			ClientboundPlayerInfoPacket t_packet = (ClientboundPlayerInfoPacket) packet;
			for (PlayerListEntry entry : t_packet.getEntries()) {
        		UUID uuid = entry.getProfile().getId();
				if (t_packet.getAction() == PlayerListEntryAction.ADD_PLAYER) {
					if (mappingOfflineUUIDs) {
		        		String name = entry.getProfile().getName();
	            		uuidMap.put(name, uuid);
	            		unformattedUuidMap.put(name.replaceAll("§[0-9a-fklmnor]", ""), uuid);
	            		if (uuid.equals(HashUtils.getOfflineUUID(name))) {
							loginNameMap.put(uuid, name);
	            		}
					}
            		playerList.put(uuid, PlayerData.fromEntry(entry));
				}
				else if (!playerList.containsKey(uuid)) {
					//System.err.println("Server tried to modify nonexistent player entry! This should not happen.");
					continue;
				}
				else if (t_packet.getAction() == PlayerListEntryAction.UPDATE_GAMEMODE) {
					playerList.get(uuid).setGameMode(entry.getGameMode());
				}
				else if (t_packet.getAction() == PlayerListEntryAction.UPDATE_LATENCY) {
					playerList.get(uuid).setPing(entry.getPing());
				}
				else if (t_packet.getAction() == PlayerListEntryAction.UPDATE_DISPLAY_NAME) {
					playerList.get(uuid).setDisplayName(entry.getDisplayName());
				}
				else if (t_packet.getAction() == PlayerListEntryAction.REMOVE_PLAYER) {
					playerList.remove(uuid);
				}
        	}
		}
	}
	
	public void onDisconnected(DisconnectedEvent event) {
		playerList.clear();
	}
	
	public PlayerData getPlayerEntry(UUID uuid) {
		return playerList.get(uuid);
	}
	
	public boolean isOnline(UUID uuid) {
		return playerList.containsKey(uuid);
	}
	
	public UUID getRecordedUUID(String username) {
		UUID uuid = uuidMap.get(username);
		if (uuid == null) {
			uuid = unformattedUuidMap.get(username.replaceAll("§.", ""));
		}
		return uuid;
	}
	
	public String getRecordedLoginName(UUID uuid) {
		return loginNameMap.get(uuid);
	}
}
