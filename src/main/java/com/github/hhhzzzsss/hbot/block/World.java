package com.github.hhhzzzsss.hbot.block;

import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.level.block.BlockChangeEntry;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import com.github.steveice10.opennbt.conversion.builtin.IntTagConverter;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class World implements PacketListener, DisconnectListener {

	public HashMap<ChunkPos, ChunkColumn> chunks = new HashMap<>();
	@Getter private int height = 256;
	@Getter private int minY = 0;
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundLoginPacket) {
			ClientboundLoginPacket t_packet = (ClientboundLoginPacket) packet;
			CompoundTag dimensionEntry = t_packet.getDimension();
			height = ((IntTag)dimensionEntry.get("height")).getValue();
			minY = ((IntTag)dimensionEntry.get("min_y")).getValue();
		} else if (packet instanceof ClientboundRespawnPacket) {
			ClientboundRespawnPacket t_packet = (ClientboundRespawnPacket) packet;
			CompoundTag dimensionEntry = t_packet.getDimension();
			height = ((IntTag)dimensionEntry.get("height")).getValue();
			minY = ((IntTag)dimensionEntry.get("min_y")).getValue();
			chunks.clear();
		} if (packet instanceof ClientboundLevelChunkWithLightPacket) {
			ClientboundLevelChunkWithLightPacket t_packet = (ClientboundLevelChunkWithLightPacket) packet;
			ChunkPos pos = new ChunkPos(t_packet.getX(), t_packet.getZ());
			ChunkColumn column;
			try {
				column = new ChunkColumn(pos, t_packet.getChunkData(), height, minY);
			} catch (IOException e) {
				return;
			}
			chunks.put(pos, column);
//			if (!chunks.containsKey(pos) || column.getBiomeData() != null) {
//				ChunkColumn chunk = new ChunkColumn(pos);
//				chunk.loadColumn(column);
//				chunks.put(pos, chunk);
//			}
//			else {
//				ChunkColumn chunk = chunks.get(pos);
//				chunk.loadColumn(column);
//			}
		}
		else if (packet instanceof ClientboundBlockUpdatePacket) {
			ClientboundBlockUpdatePacket t_packet = (ClientboundBlockUpdatePacket) packet;
			Position pos = t_packet.getEntry().getPosition();
			int id = t_packet.getEntry().getBlock();
			setBlock(pos.getX(), pos.getY(), pos.getZ(), id);
		}
		else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
			ClientboundSectionBlocksUpdatePacket t_packet = (ClientboundSectionBlocksUpdatePacket) packet;
			for (BlockChangeEntry bcr : t_packet.getEntries()) {
				Position pos = bcr.getPosition();
				int id = bcr.getBlock();
				setBlock(pos.getX(), pos.getY(), pos.getZ(), id);
			}
		}
		else if (packet instanceof ClientboundForgetLevelChunkPacket) {
			ClientboundForgetLevelChunkPacket t_packet = (ClientboundForgetLevelChunkPacket) packet;
			chunks.remove(new ChunkPos(t_packet.getX(), t_packet.getZ()));
		}
	}
	
	@Override
	public void onDisconnected(DisconnectedEvent event) {
		chunks.clear();
	}

	public ChunkColumn getChunk(int x, int z) {
		return chunks.get(new ChunkPos(x, z));
	}
	
	public ChunkColumn getChunk(ChunkPos pos) {
		return chunks.get(pos);
	}
	
	public Collection<ChunkColumn> getChunks() {
		return chunks.values();
	}
	
	public int getBlock(int x, int y, int z) {
		ChunkPos chunkPos = new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
		ChunkColumn chunk = chunks.get(chunkPos);
		return chunk == null ? 0 : chunks.get(chunkPos).getBlock(x&15, y, z&15);
	}
	
	public void setBlock(int x, int y, int z, int id) {
		ChunkPos chunkPos = new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
		if (!chunks.containsKey(chunkPos)) {
			System.err.println("Tried to set block in nonexistent chunk! This should not happen.");
			return;
		}
		chunks.get(chunkPos).setBlock(x&15, y, z&15, id);
	}
}
