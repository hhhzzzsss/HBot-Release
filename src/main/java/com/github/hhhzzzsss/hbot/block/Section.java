package com.github.hhhzzzsss.hbot.block;

import java.util.ArrayList;
import java.util.List;

import com.github.hhhzzzsss.hbot.util.Vec3i;

import lombok.*;

public class Section {
	@Getter private final int xdim;
	@Getter private final int ydim;
	@Getter private final int zdim;
	@Getter private int[] blocks;
	@Getter private ArrayList<String> idMap = new ArrayList<>();
	@Getter private int xorig = 0;
	@Getter private int yorig = 0;
	@Getter private int zorig = 0;
	
	public Section(int xdim, int ydim, int zdim, Iterable<String> flags) {
		this.xdim = xdim;
		this.ydim = ydim;
		this.zdim = zdim;
		this.blocks = new int[xdim*ydim*zdim];
		loadFlags(flags);
	}
	
	public String getBlock(int i) {
		return idMap.get(blocks[i]);
	}
	
	public String getBlock(int x, int y, int z) {
		return idMap.get(blocks[index(x, y, z)]);
	}
	
	public void setId(int i, int id) {
		blocks[i] = id;
	}
	
	public void setId(int x, int y, int z, int id) {
		blocks[index(x, y, z)] = id;
	}
	
	public int addPaletteEntry(String block) {
		if (block.startsWith("minecraft:")) block = block.substring(10);
		block = block.replaceAll("(.*leaves)\\[.*\\]", "$1[persistent=true]");
		idMap.add(block);
		return idMap.size()-1;
	}
	
	public void setPaletteEntries(String... blocks) {
		idMap.clear();
		for (String block : blocks) {
			addPaletteEntry(block);
		}
	}
	
	public void setPaletteEntries(Iterable<String> blocks) {
		idMap.clear();
		for (String block : blocks) {
			addPaletteEntry(block);
		}
	}
	
	public void setOrigin(int x, int y, int z) {
		xorig = x;
		yorig = y;
		zorig = z;
	}
	
	public int index(int x, int y, int z) {
		return y*xdim*zdim + z*xdim + x;
	}
	
	public Vec3i decomposeIndex(int index) {
		int x = index % xdim;
		index /= xdim;
		int z = index % zdim;
		index /= zdim;
		int y = index;
		return new Vec3i(x, y, z);
	}
	
	public int size() {
		return xdim*ydim*zdim;
	}
	
	public static enum SectionFlag {
		CENTERED,
		HCENTERED,
		VCENTERED,
		FILLAIR,
	}
	private boolean[] flags = new boolean[SectionFlag.values().length];
	
	public boolean hasFlag(SectionFlag flag) {
		return flags[flag.ordinal()];
	}
	
	private void setFlag(SectionFlag flag, boolean state) {
		flags[flag.ordinal()] = state;
	}
	
	private void loadFlags(Iterable<String> flags) {
		for (String flag : flags) {
			try {
				setFlag(SectionFlag.valueOf(flag.toUpperCase()), true);
			}
			catch (IllegalArgumentException e) {}	
		}
	}
	
	@AllArgsConstructor
	private class Subdivision {
		public int x1, y1, z1, x2, y2, z2, id;
	}
	
	public ArrayList<Subdivision> subdivisions;
	
	public void cubicOptimize(int maxSize) {
		subdivisions = new ArrayList<>();
		boolean included[] = new boolean[size()];
		for (int i=0; i<size(); i++) {
			if (included[i]) continue;
			if (!hasFlag(SectionFlag.FILLAIR) && getBlock(i).equals("air")) continue;
			
			int blockId = blocks[i];
			Vec3i xyz = decomposeIndex(i);
			Subdivision subdiv = new Subdivision(xyz.getX(), xyz.getY(), xyz.getZ(), xyz.getX(), xyz.getY(), xyz.getZ(), blockId);
			boolean canExpandX = true;
			boolean canExpandY = true;
			boolean canExpandZ = true;
			while (canExpandX || canExpandY || canExpandZ) {
				int xExpandedSize = (subdiv.x2-subdiv.x1+2)*(subdiv.y2-subdiv.y1+1)*(subdiv.z2-subdiv.z1+1);
				int yExpandedSize = (subdiv.x2-subdiv.x1+1)*(subdiv.y2-subdiv.y1+2)*(subdiv.z2-subdiv.z1+1);
				int zExpandedSize = (subdiv.x2-subdiv.x1+1)*(subdiv.y2-subdiv.y1+2)*(subdiv.z2-subdiv.z1+2);
				if (xExpandedSize > maxSize || subdiv.x2 == xdim-1) canExpandX = false;
				if (yExpandedSize > maxSize || subdiv.y2 == ydim-1) canExpandY = false;
				if (zExpandedSize > maxSize || subdiv.z2 == zdim-1) canExpandZ = false;
				if (canExpandX) {
					for (int y=subdiv.y1; y<=subdiv.y2; y++) for (int z=subdiv.z1; z<=subdiv.z2; z++) {
						int subIdx = index(subdiv.x2+1, y, z);
						if (blocks[subIdx] != blockId || included[subIdx]) {
							canExpandX = false;
							break;
						}
					}
				}
				if (canExpandY) {
					for (int x=subdiv.x1; x<=subdiv.x2; x++) for (int z=subdiv.z1; z<=subdiv.z2; z++) {
						int subIdx = index(x, subdiv.y2+1, z);
						if (blocks[subIdx] != blockId || included[subIdx]) {
							canExpandY = false;
							break;
						}
					}
				}
				if (canExpandZ) {
					for (int x=subdiv.x1; x<=subdiv.x2; x++) for (int y=subdiv.y1; y<=subdiv.y2; y++) {
						int subIdx = index(x, y, subdiv.z2+1);
						if (blocks[subIdx] != blockId || included[subIdx]) {
							canExpandZ = false;
							break;
						}
					}
				}
				
				int maxExpand = 0;
				int expandIdx = -1;
				if (canExpandX && xExpandedSize > maxExpand) {
					maxExpand = xExpandedSize;
					expandIdx = 0;
				}
				if (canExpandY && yExpandedSize > maxExpand) {
					maxExpand = yExpandedSize;
					expandIdx = 1;
				}
				if (canExpandZ && zExpandedSize > maxExpand) {
					maxExpand = zExpandedSize;
					expandIdx = 2;
				}
				
				if (expandIdx == 0) {
					subdiv.x2++;
				}
				else if (expandIdx == 1) {
					subdiv.y2++;
				}
				else if (expandIdx == 2) {
					subdiv.z2++;
				}
			}
			for (int x=subdiv.x1; x<=subdiv.x2; x++) for (int y=subdiv.y1; y<=subdiv.y2; y++) for (int z=subdiv.z1; z<=subdiv.z2; z++) {
				included[index(x,y,z)] = true;
			}
			subdivisions.add(subdiv);
		}
	}
	
	public void hollow(int interiorId) {
		boolean[][][] flags = new boolean[xdim][ydim][zdim];
		for (int x = 1; x < xdim-1; x++) {
			for (int y = 1; y < ydim-1; y++) {
				for (int z = 1; z < zdim-1; z++) {
					if (!getBlock(x,y,z).equals("air") &&
							!getBlock(x-1,y,z).equals("air") && !getBlock(x+1,y,z).equals("air") &&
							!getBlock(x,y-1,z).equals("air") && !getBlock(x,y+1,z).equals("air") &&
							!getBlock(x,y,z-1).equals("air") && !getBlock(x,y,z+1).equals("air")) {
						flags[x][y][z] = true;
					}
				}
			}
		}
		for (int x = 1; x < xdim-1; x++) {
			for (int y = 1; y < ydim-1; y++) {
				for (int z = 1; z < zdim; z++) {
					if (flags[x][y][z]) {
						setId(x, y, z, interiorId);
					}
				}
			}
		}
	}
	
	@Getter @Setter private int index = 0;
	public String nextCommand() {
		if (subdivisions == null) {
			return nextUnoptimizedCommand();
		}
		else {
			return nextOptimizedCommand();
		}
	}
	
	private String nextUnoptimizedCommand() {
		if (!hasFlag(SectionFlag.FILLAIR)) {
			while (index < size() && getBlock(index).equals("air")) {
				index++;
			}
		}
		if (index < size()) {
			Vec3i xyz = decomposeIndex(index);
			xyz = xyz.offset(xorig, yorig, zorig);
			if (hasFlag(SectionFlag.HCENTERED) || hasFlag(SectionFlag.CENTERED)) {
				xyz = xyz.offset(-xdim/2, 0, -zdim/2);
			}
			if (hasFlag(SectionFlag.VCENTERED) || hasFlag(SectionFlag.CENTERED)) {
				xyz = xyz.offset(0, -ydim/2, 0);
			}
			
			String command = String.format("setblock %d %d %d %s replace", xyz.getX(), xyz.getY(), xyz.getZ(), getBlock(index));
			index++;
			return command;
		}
		else {
			return null;
		}
	}
	
	private String nextOptimizedCommand() {
		if (index < subdivisions.size()) {
			Subdivision subdiv = subdivisions.get(index++);
			Vec3i xyz1 = new Vec3i(subdiv.x1, subdiv.y1, subdiv.z1);
			Vec3i xyz2 = new Vec3i(subdiv.x2, subdiv.y2, subdiv.z2);
			xyz1 = xyz1.offset(xorig, yorig, zorig);
			xyz2 = xyz2.offset(xorig, yorig, zorig);
			if (hasFlag(SectionFlag.HCENTERED) || hasFlag(SectionFlag.CENTERED)) {
				xyz1 = xyz1.offset(-xdim/2, 0, -zdim/2);
				xyz2 = xyz2.offset(-xdim/2, 0, -zdim/2);
			}
			if (hasFlag(SectionFlag.VCENTERED) || hasFlag(SectionFlag.CENTERED)) {
				xyz1 = xyz1.offset(0, -ydim/2, 0);
				xyz2 = xyz2.offset(0, -ydim/2, 0);
			}
			if (subdiv.x1 == subdiv.x2 && subdiv.y1 == subdiv.y2 && subdiv.z1 == subdiv.z2) {
				return String.format("setblock %d %d %d %s replace", xyz1.getX(), xyz1.getY(), xyz1.getZ(), idMap.get(subdiv.id));
			}
			else {
				return String.format("fill %d %d %d %d %d %d %s replace", xyz1.getX(), xyz1.getY(), xyz1.getZ(), xyz2.getX(), xyz2.getY(), xyz2.getZ(), idMap.get(subdiv.id));
			}
		}
		else {
			return null;
		}
	}
}
