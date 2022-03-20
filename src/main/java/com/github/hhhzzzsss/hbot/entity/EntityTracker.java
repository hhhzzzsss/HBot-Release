package com.github.hhhzzzsss.hbot.entity;

import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddMobPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddPlayerPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EntityTracker implements PacketListener, DisconnectListener {
	@Getter private ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
	@Getter private Map<Integer, Entity> entityMap = Collections.synchronizedMap(new HashMap<Integer, Entity>());
	@Getter private Map<UUID, Entity> uuidMap = Collections.synchronizedMap(new HashMap<UUID, Entity>());
	@Getter @Setter private boolean trackEntities = true;
	@Getter @Setter private boolean trackLiving = true;
	@Getter @Setter private boolean trackPlayers = true;
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundAddEntityPacket) {
			if (trackEntities) {
				ClientboundAddEntityPacket t_packet = (ClientboundAddEntityPacket) packet;
				Entity entity = new Entity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ClientboundAddMobPacket) {
			if (trackLiving) {
				ClientboundAddMobPacket t_packet = (ClientboundAddMobPacket) packet;
				Entity entity = new LivingEntity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ClientboundAddPlayerPacket) {
			if (trackPlayers) {
				ClientboundAddPlayerPacket t_packet = (ClientboundAddPlayerPacket) packet;
				Entity entity = new PlayerEntity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ClientboundMoveEntityPosPacket) {
			ClientboundMoveEntityPosPacket t_packet = (ClientboundMoveEntityPosPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundMoveEntityPosRotPacket) {
			ClientboundMoveEntityPosRotPacket t_packet = (ClientboundMoveEntityPosRotPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundMoveEntityRotPacket) {
			ClientboundMoveEntityRotPacket t_packet = (ClientboundMoveEntityRotPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundRotateHeadPacket) {
			ClientboundRotateHeadPacket t_packet = (ClientboundRotateHeadPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundTeleportEntityPacket) {
			ClientboundTeleportEntityPacket t_packet = (ClientboundTeleportEntityPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundSetEntityMotionPacket) {
			ClientboundSetEntityMotionPacket t_packet = (ClientboundSetEntityMotionPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ClientboundEntityEventPacket) {
			ClientboundEntityEventPacket t_packet = (ClientboundEntityEventPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		if (packet instanceof ClientboundRemoveEntitiesPacket) {
			ClientboundRemoveEntitiesPacket t_packet = (ClientboundRemoveEntitiesPacket) packet;
			for (int eid : t_packet.getEntityIds()) {
				removeEntity(eid);
			}
		}
	}
	
	private void addEntity(Entity entity) {
		entityMap.put(entity.getEid(), entity);
		uuidMap.put(entity.getUuid(), entity);
	}
	
	private void removeEntity(int eid) {
		Entity entity = entityMap.get(eid);
		if (entity != null) {
			entity.deleted = true;
			entityMap.remove(eid);
			uuidMap.remove(entity.getUuid());
		}
	}
	
	@Override
	public void onDisconnected(DisconnectedEvent event) {
		entityMap.clear();
	}
	
	public Entity getEntity(int eid) {
		return entityMap.get(eid);
	}
	
	public Entity getEntityByUuid(UUID uuid) {
		return uuidMap.get(uuid);
	}
}
