package com.github.hhhzzzsss.hbot.processes.treegen;

import java.util.ArrayList;

import com.github.hhhzzzsss.hbot.util.kdtree.KdTree;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.util.Vec3d;

public class SkeletonGenerator {
	public static ArrayList<TreeNode> generateOak(TreeProperties properties, Vec3d origin, PlatformInfo platform) {
		ArrayList<AttractorNode> branchAttractors = EnvelopeGenerator.generateOak(origin.add(0.0, properties.size/6.0, 0.0), properties.size, (int) Math.pow(Math.round(properties.size/10), 3));
		ArrayList<TreeNode> branchNodes = new ArrayList<TreeNode>();
		branchNodes.add(new TreeNode(origin));
		KdTree branchkdt = new KdTree(3, (ArrayList)branchNodes);
		for (int i=0; i<500; i++) {
			if (iterate(branchNodes, branchAttractors, branchkdt, properties.size*2.0, 5.0)) {
				platform.sendMessage("&2Finished calculating branches on iteration &3" + i);
				break;
			}
		}
		
		ArrayList<AttractorNode> rootAttractors = EnvelopeGenerator.generateRoot(origin, properties.size/2, (int) Math.pow(Math.round(properties.size/10), 2));
		ArrayList<TreeNode> rootNodes = new ArrayList<TreeNode>();
		rootNodes.add(new TreeNode(origin));
		KdTree rootkdt = new KdTree(3, (ArrayList)rootNodes);
		for (int i=0; i<500; i++) {
			if (iterate(rootNodes, rootAttractors, rootkdt, properties.size*2.0, 5.0)) {
				platform.sendMessage("&2Finished calculating roots on iteration &3" + i);
				break;
			}
		}
		
		if (properties.bare) {
			branchNodes.get(0).disableLeaves();
		}
		rootNodes.get(0).disableLeaves();
		
		double trunkArea = Math.PI * Math.pow(properties.size/20, 2);
		branchNodes.get(0).calcUnscaledArea(0);
		branchNodes.get(0).scaleArea(trunkArea / branchNodes.get(0).area);
		rootNodes.get(0).calcUnscaledArea(0.08);
		rootNodes.get(0).scaleArea(2.0*trunkArea / rootNodes.get(0).area);
		
		branchNodes.addAll(rootNodes);
		return branchNodes;
	}
	
	public static ArrayList<TreeNode> generatePlasmaBall(TreeProperties properties, Vec3d origin, PlatformInfo platform) {
		ArrayList<AttractorNode> attractors = EnvelopeGenerator.generateSphereSurface(origin, properties.size, (int) Math.pow(Math.round(properties.size/2), 2));
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(new TreeNode(origin));
		KdTree kdt = new KdTree(3, (ArrayList)nodes);
		for (int i=0; i<500; i++) {
			if (iterate(nodes, attractors, kdt, properties.size*2.0, 2.0)) {
				platform.sendMessage("&2Finished calculating branches on iteration &3" + i);
				break;
			}
		}
		nodes.get(0).disableLeaves();
		
		double trunkArea = Math.PI * Math.pow(properties.size/10, 2);
		nodes.get(0).calcUnscaledArea(0);
		nodes.get(0).scaleArea(trunkArea / nodes.get(0).area);
		
		return nodes;
	}
	
	public static ArrayList<TreeNode> generateRootBall(TreeProperties properties, Vec3d origin, PlatformInfo platform) {
		ArrayList<AttractorNode> attractors = EnvelopeGenerator.generateSphere(origin, properties.size, (int) Math.pow(Math.round(properties.size/8), 3));
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(new TreeNode(origin));
		KdTree kdt = new KdTree(3, (ArrayList)nodes);
		for (int i=0; i<500; i++) {
			if (iterate(nodes, attractors, kdt, properties.size/10.0, 3.0)) {
				platform.sendMessage("&2Finished calculating branches on iteration &3" + i);
				break;
			}
		}
		nodes.get(0).disableLeaves();
		
		double trunkArea = Math.PI * Math.pow(properties.size/10, 2);
		nodes.get(0).calcUnscaledArea(0);
		nodes.get(0).scaleArea(trunkArea / nodes.get(0).area);
		
		return nodes;
	}
	
	public static ArrayList<TreeNode> generateBonsai(TreeProperties properties, Vec3d origin, PlatformInfo platform) {
		ArrayList<AttractorNode> branchAttractors = EnvelopeGenerator.generateBonsai(origin.add(0.0, properties.size/8.0, 0.0), properties.size, properties.size, 5, (int) Math.pow(Math.round(properties.size/15), 3));
		ArrayList<TreeNode> branchNodes = new ArrayList<TreeNode>();
		branchNodes.add(new TreeNode(origin, new Vec3d(1.0, 2.0, 1.0)));
		KdTree branchkdt = new KdTree(3, (ArrayList)branchNodes);
		for (int i=0; i<500; i++) {
			if (iterate(branchNodes, branchAttractors, branchkdt, properties.size*2.0, 5.0)) {
				platform.sendMessage("&2Finished calculating branches on iteration &3" + i);
				break;
			}
		}
		
		ArrayList<AttractorNode> rootAttractors = EnvelopeGenerator.generateRoot(origin, properties.size/2, (int) Math.pow(Math.round(properties.size/10), 2));
		ArrayList<TreeNode> rootNodes = new ArrayList<TreeNode>();
		rootNodes.add(new TreeNode(origin));
		KdTree rootkdt = new KdTree(3, (ArrayList)rootNodes);
		for (int i=0; i<500; i++) {
			if (iterate(rootNodes, rootAttractors, rootkdt, properties.size*2.0, 5.0)) {
				platform.sendMessage("&2Finished calculating roots on iteration &3" + i);
				break;
			}
		}
		
		if (properties.bare) {
			branchNodes.get(0).disableLeaves();
		}
		rootNodes.get(0).disableLeaves();
		
		double trunkArea = Math.PI * Math.pow(properties.size/20, 2);
		branchNodes.get(0).calcUnscaledArea(0);
		branchNodes.get(0).scaleArea(trunkArea / branchNodes.get(0).area);
		rootNodes.get(0).calcUnscaledArea(0.08);
		rootNodes.get(0).scaleArea(2.0*trunkArea / rootNodes.get(0).area);
		
		branchNodes.addAll(rootNodes);
		return branchNodes;
	}
	
	public static ArrayList<TreeNode> generateInfestedBall(TreeProperties properties, Vec3d origin, PlatformInfo platform) {
		ArrayList<AttractorNode> attractors = EnvelopeGenerator.generateSphereSurface(origin, properties.size, (int) Math.pow(Math.round(properties.size/2), 2));
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(new TreeNode(origin.add(0.0, properties.size/2.0, 0.0)));
		KdTree kdt = new KdTree(3, (ArrayList)nodes);
		for (int i=0; i<500; i++) {
			if (iterate(nodes, attractors, kdt, properties.size/10, 2.0)) {
				platform.sendMessage("&2Finished calculating branches on iteration &3" + i);
				break;
			}
		}
		//nodes.get(0).disableLeaves();
		
		double trunkArea = Math.PI * Math.pow(properties.size/10, 2);
		nodes.get(0).calcUnscaledArea(0);
		nodes.get(0).scaleArea(trunkArea / nodes.get(0).area);
		
		return nodes;
	}
	
	public static boolean iterate(ArrayList<TreeNode> nodes, ArrayList<AttractorNode> attractors, KdTree kdt, double di, double dk) {
		for (int i=0; i<attractors.size(); i++) {
			TreeNode nearest = (TreeNode) kdt.findNearest(attractors.get(i));
			double dist = nearest.pos.subtract(attractors.get(i).pos).length();
			if (dist < dk) {
				attractors.remove(i);
			}
		}
		if (attractors.size() == 0) {
			return true;
		}
		
		for (AttractorNode attractor : attractors) {
			TreeNode nearest = (TreeNode) kdt.findNearest(attractor);
			double dist = nearest.pos.subtract(attractor.pos).length();
			if (dist < di) {
				nearest.addAttractor(attractor);
			}
		}
		boolean changed = false;
		int numNodes = nodes.size();
		for (int i=0; i<numNodes; i++) {
			if (nodes.get(i).tryGrow(nodes, kdt)) {
				changed = true;
			}
		}
		if (!changed) {
			return true;
		}
		
		return false;
	}
}
