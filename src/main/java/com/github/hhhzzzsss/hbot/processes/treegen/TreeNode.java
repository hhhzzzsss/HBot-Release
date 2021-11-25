package com.github.hhhzzzsss.hbot.processes.treegen;

import java.util.ArrayList;

import com.github.hhhzzzsss.hbot.util.kdtree.KdTree;
import com.github.hhhzzzsss.hbot.util.kdtree.Node;

import com.github.hhhzzzsss.hbot.util.Vec3d;

public class TreeNode extends Node {
	public Vec3d pos;
	public Vec3d attraction = Vec3d.ZERO;
	public ArrayList<TreeNode> children = new ArrayList<>();
	public double area = 0;
	public boolean growLeaves = true;
	public Vec3d scaling;
	
	public TreeNode(Vec3d pos) {
		super(pos.getX(), pos.getY(), pos.getZ());
		scaling = new Vec3d(1.0, 1.0, 1.0);
		this.pos = pos;
	}
	
	public TreeNode(Vec3d pos, Vec3d scaling) {
		super(pos.getX(), pos.getY(), pos.getZ());
		this.pos = pos;
		this.scaling = scaling;
	}
	
	public void addAttractor(AttractorNode attractor) {
		this.attraction = this.attraction.add(attractor.pos.subtract(this.pos).normalize());
	}
	
	public boolean tryGrow(ArrayList<TreeNode> nodes, KdTree kdt) {
		if (!this.attraction.equals(Vec3d.ZERO)) {
			Vec3d newPos = this.pos.add(this.attraction.normalize());
			for (TreeNode child : children) {
				if (child.pos.subtract(newPos).lengthSquared() < 0.0001) {
					return false;
				}
			}
			TreeNode newNode = new TreeNode(newPos, scaling);
			nodes.add(newNode);
			kdt.insert(newNode);
			this.children.add(newNode);
			attraction = Vec3d.ZERO;
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isTip() {
		return this.children.size() == 0;
	}
	
	public boolean isLeaf() {
		return this.isTip() && this.growLeaves;
	}
	
	public void disableLeaves() {
		this.growLeaves = false;
		for (TreeNode child : children) {
			child.disableLeaves();
		}
	}
	
	public double calcUnscaledArea(double decayFactor) {
		if (this.isTip()) {
			this.area = 1.0;
			return this.area;
		}
		this.area = 0.0;
		for (TreeNode child : children) {
			this.area += (1.0+decayFactor) * child.calcUnscaledArea(decayFactor);
		}
		return this.area;
	}
	
	public void scaleArea(double factor) {
		this.area *= factor;
		for (TreeNode child : children) {
			child.scaleArea(factor);
		}	
	}
	
	@Override
	public double distance(Node node) {
        double dx = get(0)-node.get(0);
        double dy = get(1)-node.get(1);
        double dz = get(2)-node.get(2);
        
        return (new Vec3d(dx, dy, dz)).multiply(scaling).lengthSquared();
    }
}
