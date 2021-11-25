package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.steveice10.mc.protocol.data.MagicValues;
import com.github.steveice10.mc.protocol.data.game.entity.player.PositionElement;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;

@RequiredArgsConstructor
public class PositionManager implements PacketListener, DisconnectListener {
	private final Bot bot;
	@Getter private double x = 0;
	@Getter private double y = 0;
	@Getter private double z = 0;
	@Getter private float yaw = 0;
	@Getter private float pitch = 0;
	@Getter private boolean spawned = false;
	
	public void move(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		bot.sendPacket(new ClientPlayerPositionRotationPacket(true, x, y, z, yaw, pitch));
	}
	
	public void moveLook(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		bot.sendPacket(new ClientPlayerPositionRotationPacket(true, x, y, z, this.yaw, this.pitch));
	}
	
	public void look(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		bot.sendPacket(new ClientPlayerPositionRotationPacket(true, x, y, z, this.yaw, this.pitch));
	}

	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ServerPlayerPositionRotationPacket) {
			ServerPlayerPositionRotationPacket t_packet = (ServerPlayerPositionRotationPacket) packet;
			boolean[] relFlags = new boolean[5];
			for (PositionElement element : t_packet.getRelative()) {
				relFlags[MagicValues.value(Integer.class, element)] = true;
			}
        	x = relFlags[0] ? x+t_packet.getX() : t_packet.getX();
        	y = relFlags[1] ? y+t_packet.getY() : t_packet.getY();
        	z = relFlags[2] ? z+t_packet.getZ() : t_packet.getZ();
        	yaw = relFlags[3] ? yaw+t_packet.getYaw() : t_packet.getYaw();
        	pitch = relFlags[4] ? pitch+t_packet.getPitch() : t_packet.getPitch();
        	bot.sendPacket(new ClientTeleportConfirmPacket(t_packet.getTeleportId()));
        	spawned = true;
        }
	}
	
	@Override
	public void onDisconnected(DisconnectedEvent event) {
		spawned = false;
	}
}
