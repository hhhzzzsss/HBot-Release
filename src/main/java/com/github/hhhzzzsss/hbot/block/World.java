package com.github.hhhzzzsss.hbot.block;

import java.util.Collection;
import java.util.HashMap;

import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;

public class World implements PacketListener, DisconnectListener {
	
	public HashMap<ChunkPos, ChunkColumn> chunks = new HashMap<>();
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ServerChunkDataPacket) {
			ServerChunkDataPacket t_packet = (ServerChunkDataPacket) packet;
			Column column = t_packet.getColumn();
			ChunkPos pos = new ChunkPos(column.getX(), column.getZ());
			
			if (!chunks.containsKey(pos) || column.getBiomeData() != null) {
				ChunkColumn chunk = new ChunkColumn(pos);
				chunk.loadColumn(column);
				chunks.put(pos, chunk);
			}
			else {
				ChunkColumn chunk = chunks.get(pos);
				chunk.loadColumn(column);
			}
		}
		else if (packet instanceof ServerBlockChangePacket) {
			ServerBlockChangePacket t_packet = (ServerBlockChangePacket) packet;
			Position pos = t_packet.getRecord().getPosition();
			int id = t_packet.getRecord().getBlock();
			setBlock(pos.getX(), pos.getY(), pos.getZ(), id);
		}
		else if (packet instanceof ServerMultiBlockChangePacket) {
			ServerMultiBlockChangePacket t_packet = (ServerMultiBlockChangePacket) packet;
			for (BlockChangeRecord bcr : t_packet.getRecords()) {
				Position pos = bcr.getPosition();
				int id = bcr.getBlock();
				setBlock(pos.getX(), pos.getY(), pos.getZ(), id);
			}
		}
		else if (packet instanceof ServerUnloadChunkPacket) {
			ServerUnloadChunkPacket t_packet = (ServerUnloadChunkPacket) packet;
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
