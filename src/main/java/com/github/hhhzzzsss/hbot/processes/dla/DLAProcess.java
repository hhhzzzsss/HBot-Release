package com.github.hhhzzzsss.hbot.processes.dla;

import com.github.hhhzzzsss.hbot.block.ChunkPos;

import java.util.ArrayList;
import java.util.Random;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.BlockSelector;
import com.github.hhhzzzsss.hbot.block.ChunkColumn;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.hhhzzzsss.hbot.util.Vec3i;

import lombok.Getter;

public abstract class DLAProcess extends CoreProcess {
	protected static final int maxChunkDist = 5;
	
	protected HBot hbot;
	protected String[] materials;
	protected BlockSelector selector;
	protected int centerChunkX;
	protected int centerChunkZ;
	protected int originX;
	protected int originZ;
	private boolean loading = true;
	
	protected int blockLength = 16*(maxChunkDist*2+1);
	/*
	 * 0 is air
	 * 1 is air adjacent to sticky
	 * 2 is solid block
	 * 
	 * Order goes [y][z][x]
	 */
	protected int[][][] blocks = new int[256][blockLength][blockLength];
	protected ArrayList<Vec3i> buildSequence = new ArrayList<Vec3i>();

	protected Thread loaderThread;
	protected Exception exception;
	
	public DLAProcess(HBot hbot, String[] materials, BlockSelector selector) throws CommandException {
		this.hbot = hbot;
		this.materials = materials;
		this.selector = selector;
		centerChunkX = (int) Math.floor(hbot.getPosManager().getX() / 16);
		centerChunkZ = (int) Math.floor(hbot.getPosManager().getZ() / 16);
		for (int cx = -maxChunkDist; cx <= maxChunkDist; cx++) for (int cz = -maxChunkDist; cz <= maxChunkDist; cz++) {
			ChunkPos cpos = new ChunkPos(centerChunkX+cx, centerChunkZ+cz);
			ChunkColumn chunk = hbot.getWorld().getChunk(cpos);
			if (chunk == null) {
				throw new CommandException("Some necessary chunks aren't loaded yet");
			}
			
			for (int y = 0; y < 256; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
				int id = chunk.getBlock(x, y, z);
				if (!BlockUtils.isAir(id)) {
					if (this.selector.matches(id)) {
						setSticky((cx+maxChunkDist)*16 + x, y, (cz+maxChunkDist)*16 + z);
					}
					else {
						setNonSticky((cx+maxChunkDist)*16 + x, y, (cz+maxChunkDist)*16 + z);
					}
				}
			}
		}

		originX = (centerChunkX-maxChunkDist)*16;
		originZ = (centerChunkZ-maxChunkDist)*16;
	}
	
	protected void setSticky(int x, int y, int z) {
		int xmin = Math.max(0, x-1);
		int xmax = Math.min(blockLength-1, x+1);
		int zmin = Math.max(0, z-1);
		int zmax = Math.min(blockLength-1, z+1);
		int ymin = Math.max(0, y-1);
		int ymax = Math.min(255, y+1);
		for (int ty=ymin; ty<=ymax; ty++) for (int tz=zmin; tz<=zmax; tz++) for (int tx=xmin; tx<=xmax; tx++) {
			blocks[ty][tz][tx] = Math.max(blocks[ty][tz][tx], 1);
		}
		
		blocks[y][z][x] = 2;
	}
	
	protected void setNonSticky(int x, int y, int z) {
		blocks[y][z][x] = 2;
	}
	
	private int index = 0;
	public void onSequence() {
		if (loading) {
			if (loaderThread.isAlive()) {
				return;
			}
			else if (exception != null) {
				hbot.sendChat("&cError while loading dla: &4" + exception);
				stop();
				return;
			}
			else {
				loading = false;
				hbot.sendChat("&7Finished generating dla fractal, now building...");
			}
		}
		
		if (index < buildSequence.size()) {
			int materialIndex = index * materials.length / buildSequence.size();
			Vec3i bp = buildSequence.get(index++);
			hbot.getCommandCore().run(String.format("setblock %d %d %d %s", originX+bp.getX(), bp.getY(), originZ+bp.getZ(), materials[materialIndex]));
		}
		else {
			hbot.sendChat("&7Finished building DLA");
			stop();
		}
	}
}
