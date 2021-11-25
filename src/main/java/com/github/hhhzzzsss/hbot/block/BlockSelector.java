package com.github.hhhzzzsss.hbot.block;

import java.util.ArrayList;

import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.hhhzzzsss.hbot.util.BlockUtils.BlockData;

public class BlockSelector {
	ArrayList<BlockData> exclude = new ArrayList<>();
	ArrayList<BlockData> include = new ArrayList<>();
	
	public BlockSelector(String selectorString) {
		if (selectorString.equalsIgnoreCase("all")) {
			return;
		}
		
		String[] split = selectorString.split(",");
		for (String blockString : split) {
			if (blockString.startsWith("!")) {
				BlockData block = BlockUtils.getBlockByName(blockString.substring(1));
				if (block == null) throw new IllegalArgumentException("Invalid block in selector string");
				exclude.add(block);
			}
			else {
				BlockData block = BlockUtils.getBlockByName(selectorString);
				if (block == null) throw new IllegalArgumentException("Invalid block in selector string");
				include.add(block);
			}
		}
	}
	
	public boolean matches(int blockState) {
		for (BlockData block : exclude) {
			if (blockState >= block.getMinStateId() && blockState <= block.getMaxStateId()) {
				return false;
			}
		}
		for (BlockData block : include) {
			if (blockState >= block.getMinStateId() && blockState <= block.getMaxStateId()) {
				return true;
			}
		}
		if (include.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
}
