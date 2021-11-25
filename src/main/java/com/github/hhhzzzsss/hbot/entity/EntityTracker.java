package com.github.hhhzzzsss.hbot.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerRemoveEntitiesPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnLivingEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;

public class EntityTracker implements PacketListener, DisconnectListener {
	@Getter private ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
	@Getter private Map<Integer, Entity> entityMap = Collections.synchronizedMap(new HashMap<Integer, Entity>());
	@Getter private Map<UUID, Entity> uuidMap = Collections.synchronizedMap(new HashMap<UUID, Entity>());
	@Getter @Setter private boolean trackEntities = true;
	@Getter @Setter private boolean trackLiving = true;
	@Getter @Setter private boolean trackPlayers = true;
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ServerSpawnEntityPacket) {
			if (trackEntities) {
				ServerSpawnEntityPacket t_packet = (ServerSpawnEntityPacket) packet;
				Entity entity = new Entity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ServerSpawnLivingEntityPacket) {
			if (trackLiving) {
				ServerSpawnLivingEntityPacket t_packet = (ServerSpawnLivingEntityPacket) packet;
				Entity entity = new LivingEntity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ServerSpawnPlayerPacket) {
			if (trackPlayers) {
				ServerSpawnPlayerPacket t_packet = (ServerSpawnPlayerPacket) packet;
				Entity entity = new PlayerEntity(t_packet);
				addEntity(entity);
			}
		}
		else if (packet instanceof ServerEntityPositionPacket) {
			ServerEntityPositionPacket t_packet = (ServerEntityPositionPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityPositionRotationPacket) {
			ServerEntityPositionRotationPacket t_packet = (ServerEntityPositionRotationPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityRotationPacket) {
			ServerEntityRotationPacket t_packet = (ServerEntityRotationPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityHeadLookPacket) {
			ServerEntityHeadLookPacket t_packet = (ServerEntityHeadLookPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityTeleportPacket) {
			ServerEntityTeleportPacket t_packet = (ServerEntityTeleportPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityVelocityPacket) {
			ServerEntityVelocityPacket t_packet = (ServerEntityVelocityPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		else if (packet instanceof ServerEntityStatusPacket) {
			ServerEntityStatusPacket t_packet = (ServerEntityStatusPacket) packet;
			Entity entity = entityMap.get(t_packet.getEntityId());
			if (entity != null) entity.update(t_packet);
		}
		if (packet instanceof ServerRemoveEntitiesPacket) {
			ServerRemoveEntitiesPacket t_packet = (ServerRemoveEntitiesPacket) packet;
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
