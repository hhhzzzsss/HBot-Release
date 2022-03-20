package com.github.hhhzzzsss.hbot.processes.schem;

import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.hhhzzzsss.hbot.util.DownloadUtils;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.*;
import lombok.Getter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class SchemLoaderThread extends Thread {
    @Getter private final String location;
    @Getter private Iterable<String> flags;
    @Getter private int x, y, z;
    private File schemPath;
    private URL schemUrl;
    @Getter private Exception exception;
    @Getter private String schemName;
    @Getter private Section section;

    boolean isUrl = false;

    public SchemLoaderThread(int x, int y, int z, String location, Iterable<String> flags) throws IOException {
        this.location = location;
        this.flags = flags;
        this.x = x;
        this.y = y;
        this.z = z;

        if (location.startsWith("http://") || location.startsWith("https://")) {
            isUrl = true;
            schemUrl = new URL(location);
        }
        else if (location.contains("/") || location.contains("\\")) {
            throw new IOException("Invalid characters in song name: " + location);
        }
        else if (getSchemFile(location).exists()) {
            schemPath = getSchemFile(location);
        }
        else if (getSchemFile(location+".schem").exists()) {
            schemPath = getSchemFile(location+".schem");
        }
        else if (getSchemFile(location+".schematic").exists()) {
            schemPath = getSchemFile(location+".schematic");
        }
        else if (getSchemFile(location+".nbt").exists()) {
            schemPath = getSchemFile(location+".nbt");
        }
        else {
            throw new IOException("Could not find schematic: " + location);
        }
    }

    @Override
    public void run() {
        try {
            byte[] bytes;
            if (isUrl) {
                bytes = DownloadUtils.DownloadToByteArray(schemUrl, 5*1024*1024);
                schemName = Paths.get(schemUrl.toURI().getPath()).getFileName().toString();
            }
            else {
                bytes = Files.readAllBytes(schemPath.toPath());
                schemName = schemPath.getName();
            }

            section = loadSchematic(new ByteArrayInputStream(bytes));

            section.setOrigin(x, y, z);
            section.cubicOptimize(256);
        }
        catch (Exception e) {
            exception = e;
        }
    }

    private File getSchemFile(String name) {
        return new File(SchemProcess.SCHEM_DIR, name);
    }

    public Section loadSchematic(InputStream in) throws IOException {
        CompoundTag nbt;
        try {
            Tag tag = NBTIO.readTag(new GZIPInputStream(in));
            if(!(tag instanceof CompoundTag)) {
                throw new IOException("Not a valid schematic");
            }
            nbt = (CompoundTag) tag;
        }
        catch (IOException e) {
            throw new IOException("Error parsing schematic");
        }

        if (nbt.contains("BlockData")) {
            return loadNewSchematic(nbt);
        }
        else if (nbt.contains("Blocks")) {
            return getOldSchematic(nbt);
        }
        else if (nbt.contains("blocks")) {
            return loadStructureSchematic(nbt);
        }
        else {
            throw new IOException("Not a valid schematic");
        }
    }

    private Section loadNewSchematic(CompoundTag nbt) {
        ShortTag widthtag = nbt.get("Width");
        ShortTag heighttag = nbt.get("Height");
        ShortTag lengthtag = nbt.get("Length");
        int width = widthtag.getValue();
        int height = heighttag.getValue();
        int length = lengthtag.getValue();
        Section section = new Section(width, height, length, flags);

        CompoundTag palette = nbt.get("Palette");
        ByteArrayTag blockdata = nbt.get("BlockData");

        String[] paletteArr = new String[palette.size()];
        int bpb = 1;
        while (palette.size() >> bpb > 0) {bpb++;}
        for (Tag paletteEntry : palette) {
            IntTag intEntry = (IntTag) paletteEntry;
            paletteArr[intEntry.getValue()] = intEntry.getName();
        }
        section.setPaletteEntries(paletteArr);

        int varInt = 0;
        int varIntLength = 0;
        int storageIdx = 0;
        for (int i = 0; i < blockdata.length(); i++) {
            varInt |= (int)(blockdata.getValue(i) & 127) << (varIntLength++ * 7);
            if ((blockdata.getValue(i) & 128) == 128) {
                continue;
            }

            section.setId(storageIdx++, varInt);

            varInt = 0;
            varIntLength = 0;
        }

        return section;
    }

    private Section getOldSchematic(CompoundTag nbt) {
        ShortTag widthtag = nbt.get("Width");
        ShortTag heighttag = nbt.get("Height");
        ShortTag lengthtag = nbt.get("Length");
        int width = widthtag.getValue();
        int height = heighttag.getValue();
        int length = lengthtag.getValue();
        Section section = new Section(width, height, length, flags);

        ByteArrayTag blocks = nbt.get("Blocks");
        ByteArrayTag data = nbt.get("Data");

        section.setPaletteEntries(BlockUtils.getLegacyIds());

        for (int i = 0; i < blocks.length(); i++) {
            int legacyId = ((blocks.getValue(i) & 0xff) << 4) + (data.getValue(i) & 0xf);
            section.setId(i, legacyId);
        }

        return section;
    }

    private Section loadStructureSchematic(CompoundTag nbt) {
        ListTag sizetag = nbt.get("size");
        IntTag widthtag = sizetag.get(0);
        IntTag heighttag = sizetag.get(1);
        IntTag lengthtag = sizetag.get(2);
        int width = widthtag.getValue();
        int height = heighttag.getValue();
        int length = lengthtag.getValue();
        Section section = new Section(width, height, length, flags);

        ListTag palette = nbt.get("palette");
        ListTag blocks = nbt.get("blocks");

        String[] paletteArr = new String[palette.size()+1];
        int bpb = 1;
        while (paletteArr.length >> bpb > 0) {bpb++;}
        paletteArr[0] = "minecraft:air";
        for (int i=0; i<palette.size(); i++) {
            CompoundTag paletteEntry = palette.get(i);
            StringTag nametag = paletteEntry.get("Name");
            String s = nametag.getValue();
            if (paletteEntry.contains("Properties")) {
                CompoundTag propertiestag = paletteEntry.get("Properties");
                s += "[";
                String[] properties = new String[propertiestag.size()];
                int j=0;
                for (Tag propertytag : propertiestag) {
                    properties[j] = propertytag.getName() + "=" + (String) propertytag.getValue();
                    j++;
                }
                s += String.join(",", properties);
                s += "]";
            }
            paletteArr[i+1] = s;
        }
        section.setPaletteEntries(paletteArr);

        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag blockTag = blocks.get(i);
            IntTag stateTag = blockTag.get("state");
            ListTag posTag = blockTag.get("pos");
            IntTag xTag = posTag.get(0);
            IntTag yTag = posTag.get(1);
            IntTag zTag = posTag.get(2);
            int x = xTag.getValue();
            int y = yTag.getValue();
            int z = zTag.getValue();
            int storageIdx = width*length*y + width*z + x;

            section.setId(storageIdx, stateTag.getValue()+1);
        }

        return section;
    }
}
