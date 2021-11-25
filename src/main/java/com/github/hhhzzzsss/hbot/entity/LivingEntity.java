package com.github.hhhzzzsss.hbot.entity;

import com.github.steveice10.mc.protocol.data.game.entity.EntityStatus;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnLivingEntityPacket;
import lombok.*;

@Getter
public class LivingEntity extends Entity {
	protected double headYaw;
	protected boolean alive = true;
	
	protected LivingEntity() {}
	
	public LivingEntity(ServerSpawnLivingEntityPacket p) {
		type = p.getType();
		eid = p.getEntityId();
		uuid = p.getUuid();
		x = p.getX();
		y = p.getY();
		z = p.getZ();
		yaw = p.getYaw();
		pitch = p.getPitch();
		headYaw = p.getHeadYaw();
		xVel = p.getMotionX();
		yVel = p.getMotionY();
		zVel = p.getMotionZ();
	}
	
	public void update(ServerEntityHeadLookPacket p) {
		headYaw = p.getHeadYaw();
	}
	
	public void update(ServerEntityStatusPacket p) {
		if (p.getStatus() == EntityStatus.LIVING_DEATH) {
			alive = false;
		}
	}
	
	/*public double getHeight() {
		switch (type) {
		case ARMOR_STAND:
			return 1.975;
		case BAT:
			return 0.9;
		case BEE:
			return 0.6;
		case BOAT:
			return 0.5625;
		case BLAZE:
			return 1.8;
		case CAT:
			return 0.7;
		case CAVE_SPIDER:
			return 0.5;
		case CHICKEN:
			return 0.8;
		case COD:
			return 0.3;
		case COW:
			return 1.4;
		case CREEPER:
			return 1.7;
		case DONKEY:
			return 1.5;
		case DOLPHIN:
			return 0.6;
		case DROWNED:
			return 1.95;
		case ELDER_GUARDIAN:
			return 1.9975;
		case ENDER_DRAGON:
			return 6.0;
		case ENDERMAN:
			return 2.9;
		case ENDERMITE:
			return 0.3;
		case EVOKER:
			return 1.95;
		case FOX:
			return 0.6;
		case GHAST:
			return 4.0;
		case GIANT:
			return 12;
		case GUARDIAN:
			return 0.85;
		case HORSE:
			return 1.6;
		case HUSK:
			return 1.95;
		case ILLUSIONER:
			return 1.95;
		case LLAMA:
			return 1.87;
		case MAGMA_CUBE:
			return 0.5202;
		case MINECART:
			return 0.7;
		case MINECART_CHEST:
			return 0.7;
		case MINECART_COMMAND_BLOCK:
			return 0.7;
		case MINECART_FURNACE:
			return 0.7;
		case MINECART_HOPPER:
			return 0.7;
		case MINECART_SPAWNER:
			return 0.7;
		case MINECART_TNT:
			return 0.7;
		case MULE:
			return 1.6;
		case MOOSHROOM:
			return 1.4;
		case OCELOT:
			return 0.7;
		case PANDA:
			return 1.25;
		case PARROT:
			return 0.9;
		case PUFFERFISH:
			return 0.35;
		case PIG:
			return 0.9;
		case PIGLIN:
			return 1.95;
		case PIGLIN_BRUTE:
			return 1.95;
		case POLAR_BEAR:
			return 1.4;
		case RABBIT:
			return 0.5;
		case SALMON:
			return 0.4;
		case SHEEP:
			return 1.3;
		case SHULKER:
			return 1.0;
		case SILVERFISH:
			return 0.3;
		case SKELETON:
			return 1.99;
		case SKELETON_HORSE:
			return 1.6;
		case SLIME:
			return 0.52;
		case SNOW_GOLEM:
			return 1.9;
		case SPIDER:
			return 0.9;
		case SQUID:
			return 0.8;
		case STRAY:
			return 0.99;
		case TRADER_LLAMA:
			return 1.87;
		case TROPICAL_FISH:
			return 0.4;
		case TURTLE:
			return 0.4;
		case VEX:
			return 0.8;
		case VILLAGER:
			return 1.95;
		case IRON_GOLEM:
			return 2.7;
		case VINDICATOR:
			return 1.95;
		case PILLAGER:
			return 1.95;
		case WANDERING_TRADER:
			return 1.95;
		case WITCH:
			return 1.95;
		case WITHER:
			return 3.5;
		case WITHER_SKELETON:
			return 2.4;
		case WOLF:
			return 0.85;
		case ZOMBIE:
			return 1.95;
		case ZOMBIE_HORSE:
			return 1.6;
		case ZOMBIFIED_PIGLIN:
			return 1.95;
		case ZOMBIE_VILLAGER:
			return 1.95;
		case PHANTOM:
			return 0.5;
		case RAVAGER:
			return 2.2;
		case HOGLIN:
			return 1.4;
		case STRIDER:
			return 1.7;
		case ZOGLIN:
			return 1.4;
		case PLAYER:
			return 1.8;
		default:
			return 0.0;
		}
	}*/
}
