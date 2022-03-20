package com.github.hhhzzzsss.hbot.entity;

import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddPlayerPacket;

public class PlayerEntity extends LivingEntity {
	public PlayerEntity(ClientboundAddPlayerPacket p) {
		type = EntityType.PLAYER;
		eid = p.getEntityId();
		uuid = p.getUuid();
		x = p.getX();
		y = p.getY();
		z = p.getZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		headYaw = 0.0;
		xVel = 0.0;
		yVel = 0.0;
		zVel = 0.0;
	}
}
