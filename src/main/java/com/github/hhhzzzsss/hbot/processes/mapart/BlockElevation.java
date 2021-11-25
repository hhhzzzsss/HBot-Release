package com.github.hhhzzzsss.hbot.processes.mapart;

public class BlockElevation {
	public String block;
	public int tone;
	public int elevation;
	
	public BlockElevation(String block, int tone) {
		this.block = block;
		this.tone = tone;
		this.elevation = 1; // Blocks at 0 level appear as void so I set the base elevation to 1
	}
}
