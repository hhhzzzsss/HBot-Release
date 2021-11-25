package com.github.hhhzzzsss.hbot.processes.treegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeMaterials {
	private static ArrayList<String> materials = new ArrayList<>();
	public static List<String> palette = Collections.unmodifiableList(materials);
	
	static {
		materials.add("air"); // 0
		materials.add("oak_wood"); // 1
		materials.add("oak_leaves[persistent=true]"); // 2
		materials.add("magenta_stained_glass"); // 3
		materials.add("crying_obsidian"); // 4
		materials.add("magma_block"); // 5
		materials.add("stone_bricks"); // 6
		materials.add("mossy_stone_bricks"); // 7
		materials.add("cracked_stone_bricks"); // 8
	}
	
	public static String get(int i) {
		return materials.get(i);
	}
}
