package com.github.hhhzzzsss.hbot.processes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class SchemProcess extends CoreProcess {
	private HBot hbot;
	private CommandCore commandCore;
	Section section;
	Iterable<String> flags;
	
	boolean loaded = false;
	
	public static final File SCHEM_DIR = new File("schematics");
	static {
		if (!SCHEM_DIR.exists()) {
			SCHEM_DIR.mkdir();
		}
	}
	
	public SchemProcess(HBot hbot, int x, int y, int z, String filename, Iterable<String> flags) throws CommandException {
		this.hbot = hbot;
		commandCore = hbot.getCommandCore();
		this.flags = flags;
		
		File schemPath = new File(SCHEM_DIR, filename);

		if (!schemPath.exists()) schemPath = new File(SCHEM_DIR, filename + ".schem");
		if (!schemPath.exists()) schemPath = new File(SCHEM_DIR, filename + ".schematic");
		if (!schemPath.exists()) schemPath = new File(SCHEM_DIR, filename + ".nbt");
		if (!schemPath.exists()) {
			throw new CommandException("Could not find file: " + filename);
		}
		try {
			loadSchematic(new FileInputStream(schemPath));
		} catch (FileNotFoundException e) {
			throw new CommandException("Could not find file: " + schemPath);
		}
		section.setOrigin(x, y, z);
		section.cubicOptimize(256);
		loaded = true;
	}
	
	public void loadSchematic(InputStream in) throws CommandException {
		CompoundTag nbt;
		try {
			Tag tag = NBTIO.readTag(new GZIPInputStream(in));
			if(!(tag instanceof CompoundTag)) {
	            throw new CommandException("Not a valid schematic");
	        }
			nbt = (CompoundTag) tag;
		}
		catch (IOException e) {
			throw new CommandException("Error parsing schematic");
		}
		
		if (nbt.contains("BlockData")) {
			loadNewSchematic(nbt);
		}
		else if (nbt.contains("Blocks")) {
			getOldSchematic(nbt);
		}
		else if (nbt.contains("blocks")) {
			loadStructureSchematic(nbt);
		}
		else {
			throw new CommandException("Not a valid schematic");
		}
	}
	
	private void loadNewSchematic(CompoundTag nbt) {
		ShortTag widthtag = nbt.get("Width");
		ShortTag heighttag = nbt.get("Height");
		ShortTag lengthtag = nbt.get("Length");
		int width = widthtag.getValue();
		int height = heighttag.getValue();
		int length = lengthtag.getValue();
		section = new Section(width, height, length, flags);
		
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
	}
	
	private void getOldSchematic(CompoundTag nbt) {
		ShortTag widthtag = nbt.get("Width");
		ShortTag heighttag = nbt.get("Height");
		ShortTag lengthtag = nbt.get("Length");
		int width = widthtag.getValue();
		int height = heighttag.getValue();
		int length = lengthtag.getValue();
		section = new Section(width, height, length, flags);

		ByteArrayTag blocks = nbt.get("Blocks");
		ByteArrayTag data = nbt.get("Data");
		
		section.setPaletteEntries(BlockUtils.getLegacyIds());
		
		for (int i = 0; i < blocks.length(); i++) {
			int legacyId = ((blocks.getValue(i) & 0xff) << 4) + (data.getValue(i) & 0xf);
			section.setId(i, legacyId);
		}
	}
	
	private void loadStructureSchematic(CompoundTag nbt) {
		ListTag sizetag = nbt.get("size");
		IntTag widthtag = sizetag.get(0);
		IntTag heighttag = sizetag.get(1);
		IntTag lengthtag = sizetag.get(2);
		int width = widthtag.getValue();
		int height = heighttag.getValue();
		int length = lengthtag.getValue();
		section = new Section(width, height, length, flags);
		
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
	}
	
	@Override
	public void onSequence() {
		if (!loaded || done) {
			return;
		}
		
		String command = section.nextCommand();
		if (command == null) {
			done = true;
			hbot.sendChat("&7Finished building");
		}
		else {
			commandCore.run(command);
		}
	}
}
