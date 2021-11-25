package com.github.hhhzzzsss.hbot.block;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;

import lombok.*;

@RequiredArgsConstructor
public class ChunkColumn {
	@Getter private final ChunkPos pos;
	@Getter private Chunk[] data = new Chunk[16];
	
	public void loadColumn(Column column) {
		Chunk[] chunks = column.getChunks();
		for (int i=0; i<16; i++) {
			if (chunks[i] != null) {
				data[i] = chunks[i];
			}
		}
	}
	
	public int getBlock(int x, int y, int z) {
		int yIdx = y>>4;
		if (data[yIdx] == null) return 0;
		return data[yIdx].get(x, y&15, z);
	}
	
	public void setBlock(int x, int y, int z, int id) {
		int yIdx = y>>4;
		if (data[yIdx] == null) {
			data[yIdx] = new Chunk();
			data[yIdx].set(0, 0, 0, 0);
		}
		data[yIdx].set(x, y&15, z, id);
	}
}
