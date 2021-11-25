package com.github.hhhzzzsss.hbot.processes.mapart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.processes.boxstructure.BoxStructure.Phase;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class MapartProcess extends CoreProcess {
	private static enum Phase {
		INIT,
		REMOVE_WATER,
		REMOVE_LAVA,
		FILL_AIR,
		MAKE_MAP,
		DONE,
	}
	
	private HBot hbot;
	private MapartLoaderThread loaderThread;
	
	private int originX;
	private int originZ;
	
	private Phase phase = Phase.INIT;
	
	private ArrayList<String> commands = new ArrayList<>();
	private int index = 0;
	
	boolean loaded = false;
	
	public MapartProcess(HBot hbot, int x, int z, String url, Iterable<String> flags) throws CommandException {
		this.hbot = hbot;
		int rawx = Math.floorDiv(x+64, 128);
		int rawz = Math.floorDiv(z+64, 128);
		originX = rawx*128 - 64;
		originZ = rawz*128 - 64;
		
		try {
			loaderThread = new MapartLoaderThread(url);
		} catch (IOException e) {
			throw new CommandException(e.getMessage());
		}
		loaderThread.start();
	}
	
	@Override
	public void onTick() {
		if (index == commands.size()) {
			commands.clear();
			index = 0;
			phase = Phase.values()[phase.ordinal() + 1];
			if (phase == Phase.REMOVE_WATER) {
				handleRemoveWater();
			}
			else if (phase == Phase.REMOVE_LAVA) {
				handleRemoveLava();
			}
			else if (phase == Phase.FILL_AIR) {
				handleFillAir();
			}
			else if (phase == Phase.MAKE_MAP) {
				// Must wait for loader thread to finish
				if (!loaderThread.isAlive()) {
					if (loaderThread.getBlocks() == null) {
						hbot.sendChat("&cError while loading mapart: &4" + loaderThread.getException().getMessage());
						stop();
						return;
					}
					else {
						handleMakeMap(loaderThread.getBlocks());
					}
				}
			}
			else { // Phase is DONE
				hbot.sendChat("&7Finished building map art");
				stop();
			}
		}
	}
	
	@Override
	public void onSequence() {
		if (index < commands.size()) {
			hbot.getCommandCore().run(commands.get(index++));
		}
	}
	
	private void handleRemoveWater() {
		hbot.sendChat("&7Removing water...");
		for (int y = 255; y >= 0; y--) for (int i = 0; i < 2; i++) {
			commands.add(String.format(
					"fill %d %d %d %d %d %d barrier replace water",
					originX + i*65 - 1, y, originZ - 2,
					originX + i*65 + 64, y, originZ + 128
					));
		}
	}
	
	private void handleRemoveLava() {
		hbot.sendChat("&7Removing lava...");
		for (int y = 255; y >= 0; y--) for (int i = 0; i < 2; i++) {
			commands.add(String.format(
					"fill %d %d %d %d %d %d barrier replace lava",
					originX + i*65 - 1, y, originZ - 2,
					originX + i*65 + 64, y, originZ + 128
					));
		}
	}
	
	private void handleFillAir() {
		hbot.sendChat("&7Clearing area...");
		for (int i = 0; i < 4; i++) for (int z = -1; z < 128; z++) {
			commands.add(String.format(
					"fill %d %d %d %d %d %d air replace",
					originX + i*32, 0, originZ + z,
					originX + i*32 + 31, 255, originZ + z
					));
		}
	}
	
	private void handleMakeMap(BlockElevation[][] blocks) {
		hbot.sendChat("&7Building map...");
		for (int x = 0; x < 128; x++) for (int z = -1; z < 128; z++) {
			BlockElevation be = blocks[x][z+1];
			commands.add(String.format("setblock %d %d %d %s", originX + x, be.elevation, originZ+z, be.block));
		}
	}
}
