package com.github.hhhzzzsss.hbot.entity;

import java.util.UUID;

import com.github.hhhzzzsss.hbot.util.EntityUtils;
import com.github.hhhzzzsss.hbot.util.EntityUtils.EntityData;
import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnEntityPacket;

import lombok.*;

@Getter
public class Entity implements Comparable<Entity> {
	protected int eid;
	protected UUID uuid;
	protected EntityType type;
	protected double x;
	protected double y;
	protected double z;
	protected float yaw;
	protected float pitch;
	protected double xVel;
	protected double yVel;
	protected double zVel;
	protected boolean onGround = false;
	protected boolean deleted = false;
	protected EntityData entityData;
	
	protected Entity() {}
	
	public Entity(ServerSpawnEntityPacket p) {
		eid = p.getEntityId();
		uuid = p.getUuid();
		type = p.getType();
		x = p.getX();
		y = p.getY();
		z = p.getZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		xVel = p.getMotionX();
		yVel = p.getMotionY();
		zVel = p.getMotionZ();
	}
	
	public void update(ServerEntityPositionPacket p) {
		x += p.getMoveX();
		y += p.getMoveY();
		z += p.getMoveZ();
		onGround = p.isOnGround();
	}
	
	public void update(ServerEntityPositionRotationPacket p) {
		x += p.getMoveX();
		y += p.getMoveY();
		z += p.getMoveZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ServerEntityRotationPacket p) {
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ServerEntityHeadLookPacket p) {}
	
	public void update(ServerEntityTeleportPacket p) {
		x = p.getX();
		y = p.getY();
		z = p.getZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ServerEntityVelocityPacket p) {
		xVel = p.getMotionX();
		yVel = p.getMotionY();
		zVel = p.getMotionZ();
	}
	
	public void update(ServerEntityStatusPacket p) {}
	
	@Override
	public int compareTo(Entity other) {
		return this.eid - other.eid;
	}
	
	public EntityData getEntityData() {
		if (entityData == null) {
			return EntityUtils.getEntityByName(type.name().toLowerCase().replace("thrown_potion", "potion"));
		}
		else {
			return entityData;
		}
	}
	
	public double getHeight() {
		EntityData data = getEntityData();
		return data == null ? 0.0 : data.getHeight();
	}
}
