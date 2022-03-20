package com.github.hhhzzzsss.hbot.entity;

import com.github.hhhzzzsss.hbot.util.EntityUtils;
import com.github.hhhzzzsss.hbot.util.EntityUtils.EntityData;
import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import lombok.Getter;

import java.util.UUID;

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
	
	public Entity(ClientboundAddEntityPacket p) {
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
	
	public void update(ClientboundMoveEntityPosPacket p) {
		x += p.getMoveX();
		y += p.getMoveY();
		z += p.getMoveZ();
		onGround = p.isOnGround();
	}
	
	public void update(ClientboundMoveEntityPosRotPacket p) {
		x += p.getMoveX();
		y += p.getMoveY();
		z += p.getMoveZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ClientboundMoveEntityRotPacket p) {
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ClientboundRotateHeadPacket p) {}
	
	public void update(ClientboundTeleportEntityPacket p) {
		x = p.getX();
		y = p.getY();
		z = p.getZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		onGround = p.isOnGround();
	}
	
	public void update(ClientboundSetEntityMotionPacket p) {
		xVel = p.getMotionX();
		yVel = p.getMotionY();
		zVel = p.getMotionZ();
	}
	
	public void update(ClientboundEntityEventPacket p) {}
	
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
