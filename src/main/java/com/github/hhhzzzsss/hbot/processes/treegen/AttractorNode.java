package com.github.hhhzzzsss.hbot.processes.treegen;

import com.github.hhhzzzsss.hbot.util.kdtree.Node;

import com.github.hhhzzzsss.hbot.util.Vec3d;

public class AttractorNode extends Node {
	public Vec3d pos;
	
	public AttractorNode(Vec3d pos) {
		super(pos.getX(), pos.getY(), pos.getZ());
		this.pos = pos;
	}
}
