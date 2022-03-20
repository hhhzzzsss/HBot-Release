package com.github.hhhzzzsss.hbot.block;

import com.github.steveice10.mc.protocol.data.game.chunk.DataPalette;
import com.github.steveice10.mc.protocol.data.game.chunk.palette.*;
import com.github.steveice10.packetlib.io.NetInput;
import lombok.*;

import java.io.IOException;

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
public class UnusedChunk {
    private static final int GLOBAL_BLOCK_PALETTE_BITS_PER_ENTRY = 15;
    private static final int GLOBAL_BIOME_PALETTE_BITS_PER_ENTRY = 6;

    private static final int AIR = 0;

    private int blockCount;
    private @NonNull DataPalette chunkData;
    private @NonNull DataPalette biomeData;

    public UnusedChunk() {
        this(0, DataPalette.createForChunk(GLOBAL_BLOCK_PALETTE_BITS_PER_ENTRY), DataPalette.createForBiome(GLOBAL_BIOME_PALETTE_BITS_PER_ENTRY));
    }

    public static UnusedChunk read(NetInput in) throws IOException {

        int blockCount = in.readShort();

        DataPalette chunkPalette = DataPalette.read(in, PaletteType.CHUNK, GLOBAL_BLOCK_PALETTE_BITS_PER_ENTRY);
        DataPalette biomePalette = DataPalette.read(in, PaletteType.BIOME, GLOBAL_BIOME_PALETTE_BITS_PER_ENTRY);
        return new UnusedChunk(blockCount, chunkPalette, biomePalette);
    }
}
