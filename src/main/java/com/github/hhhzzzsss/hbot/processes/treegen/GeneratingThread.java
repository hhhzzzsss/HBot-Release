package com.github.hhhzzzsss.hbot.processes.treegen;

import java.util.ArrayList;
import java.util.Random;

import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.util.Vec3d;

public class GeneratingThread extends Thread {
	private TreeType type;
	private Iterable<String> flags;
	private TreeProperties properties;
	private PlatformInfo platform;
	public boolean done = false;
	public Section section;
	
	public GeneratingThread(TreeType type, Iterable<String> flags, PlatformInfo platform) {
		this.type = type;
		this.flags = flags;
		this.properties = TreeProperties.fromFlags(flags);
		this.platform = platform;
	}
	
	public void run() {
		platform.sendMessage("&2Calculating Tree...");
		
		switch (this.type) {
		case OAK:
			section = generateOak();
			break;
		case PLASMA_BALL:
			section = generatePlasmaBall();
			break;
		case ROOT_BALL:
			section = generateRootBall();
			break;
		case BONSAI:
			section = generateBonsai();
			break;
		case INFESTED_BALL:
			section = generateInfestedBall();
			break;
		default:
			section = new Section(1, 1, 1, new ArrayList<String>());
			break;
		}
		
		if (properties.hollow) {
			section.hollow(0);;
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		platform.sendMessage("&2Now Building");
		
		done = true;
	}
	
	private Section generateOak() {
		int xlen = properties.size+20;
		int ylen = properties.size*3/2;
		int zlen = properties.size+20;
		Section section = new Section(xlen, ylen, zlen, flags);
		section.setPaletteEntries(TreeMaterials.palette);
		ArrayList<TreeNode> skeleton = SkeletonGenerator.generateOak(properties, new Vec3d(xlen/2, 0, zlen/2), platform);
		fillRegion(section, skeleton, 1, 2, 3, 7, true);
		return section;
	}
	
	private Section generatePlasmaBall() {
		int xlen = properties.size+20;
		int ylen = properties.size+20;
		int zlen = properties.size+20;
		Section section = new Section(xlen, ylen, zlen, flags);
		section.setPaletteEntries(TreeMaterials.palette);
		ArrayList<TreeNode> skeleton = SkeletonGenerator.generatePlasmaBall(properties, new Vec3d(xlen/2, ylen/2, zlen/2), platform);
		fillRegion(section, skeleton, 3, 3, 2, 2, false);
		return section;
	}
	
	private Section generateRootBall() {
		int xlen = properties.size+20;
		int ylen = properties.size+20;
		int zlen = properties.size+20;
		Section section = new Section(xlen, ylen, zlen, flags);
		section.setPaletteEntries(TreeMaterials.palette);
		ArrayList<TreeNode> skeleton = SkeletonGenerator.generateRootBall(properties, new Vec3d(xlen/2, ylen/2, zlen/2), platform);
		fillRegion(section, skeleton, 1, 2, 2, 2, false);
		return section;
	}
	
	private Section generateBonsai() {
		int xlen = properties.size*2;
		int ylen = properties.size*3/2;
		int zlen = properties.size*2;
		Section section = new Section(xlen, ylen, zlen, flags);
		section.setPaletteEntries(TreeMaterials.palette);
		ArrayList<TreeNode> skeleton = SkeletonGenerator.generateBonsai(properties, new Vec3d(xlen/2, 0, zlen/2), platform);
		fillRegion(section, skeleton, 1, 2, 3, 7, true);
		return section;
	}
	
	private Section generateInfestedBall() {
		int xlen = properties.size+20;
		int ylen = properties.size*3/2;
		int zlen = properties.size+20;
		Section section = new Section(xlen, ylen, zlen, flags);
		section.setPaletteEntries(TreeMaterials.palette);
		DistMap sphereMap = new DistMap(section);
		sphereMap.makeMetaball(new Vec3d(xlen/2,  ylen/2, zlen/2), properties.size/2);
		sphereMap.addNoise(4, 1);
		sphereMap.apply(new int[] {6, 7, 8});
		ArrayList<TreeNode> skeleton = SkeletonGenerator.generateInfestedBall(properties, new Vec3d(xlen/2, ylen/2, zlen/2), platform);
		fillRegion(section, skeleton, 4, 5, 1, 2, false);
		return section;
		
	}
	
	public void fillRegion(Section section, ArrayList<TreeNode> skeleton, int mat1, int mat2, int leafMin, int leafMax, boolean useNoise) {
		DistMap distMapBranch = new DistMap(section);
		DistMap distMapLeaf = new DistMap(section);
		for (TreeNode node : skeleton) {
			double radius = Math.sqrt(node.area / Math.PI);
			distMapBranch.makePoint(node.pos);
			distMapBranch.makeSphere(node.pos, radius);
			if (node.isLeaf()) {
				distMapLeaf.makeMetaball(node.pos, leafMin + (leafMax-leafMin)*Math.random());
			}
		}
		if (useNoise) {
			distMapLeaf.addNoise(2,  1);
			distMapLeaf.addNoise(4,  1);
		}
		distMapLeaf.apply(mat2);
		distMapBranch.apply(mat1);
	}
	
	private class DistMap {
		Section section;
		int xlen;
		int ylen;
		int zlen;
		double[][][] dists;
		
		public DistMap(Section section) {
			this.section = section;
			xlen = section.getXdim();
			ylen = section.getYdim();
			zlen = section.getZdim();
			dists = new double[xlen][ylen][zlen];
			
			for (int i=0; i<xlen; i++) {
				for (int j=0; j<ylen; j++) {
					for (int k=0; k<zlen; k++) {
						dists[i][j][k] = xlen + ylen + zlen;
					}
				}
			}
		}
		
		public void makeSphere(Vec3d a, double r) {
			int x1 = Math.max((int)Math.ceil(a.x-r), 0);
			int y1 = Math.max((int)Math.ceil(a.y-r), 0);
			int z1 = Math.max((int)Math.ceil(a.z-r), 0);
			int x2 = Math.min((int)Math.floor(a.x+r), xlen-1);
			int y2 = Math.min((int)Math.floor(a.y+r), ylen-1);
			int z2 = Math.min((int)Math.floor(a.z+r), zlen-1);
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						Vec3d p = new Vec3d(x, y, z);
						Vec3d dp = a.subtract(p);
						if (dp.dotProduct(dp) < r*r) {
							dists[x][y][z] = -1.0;
						}
					}
				}
			}
		}
		
		public void makeMetaball(Vec3d a, double r) {
			double k = 1;
			int x1 = Math.max((int)Math.ceil(a.x-(1+k)*r), 0);
		    int y1 = Math.max((int)Math.ceil(a.y-(1+k)*r), 0);
		    int z1 = Math.max((int)Math.ceil(a.z-(1+k)*r), 0);
		    int x2 = Math.min((int)Math.floor(a.x+(1+k)*r), xlen-1);
		    int y2 = Math.min((int)Math.floor(a.y+(1+k)*r), ylen-1);
		    int z2 = Math.min((int)Math.floor(a.z+(1+k)*r), zlen-1);
		    for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					for (int z = z1; z <= z2; z++) {
						Vec3d p = new Vec3d(x, y, z);
						Vec3d dp = a.subtract(p);
						double d = dp.length()-r;
						if (d < k*r) {
							dists[x][y][z] = Math.min(dists[x][y][z], d);
						}
					}
				}
			}
		}
		
		public void makePoint(Vec3d a) {
			int x = (int) Math.round(a.x);
			int y = (int) Math.round(a.y);
			int z = (int) Math.round(a.z);
			if (x >= 0 && y >= 0 && z >= 0 && x < xlen && y < ylen && z < zlen)
			dists[x][y][z] = -1;
		}
		
		public void addNoise(double scale, double magnitude) {
			ImprovedNoise.randomize();
			for (int x=0; x<xlen; x++) {
				for (int y=0; y<ylen; y++) {
					for (int z=0; z<zlen; z++) {
						dists[x][y][z] += magnitude * ImprovedNoise.noise(x/scale, y/scale, z/scale);
					}
				}
			}
		}
		
		public void apply(int id) {
			for (int i=0; i<xlen; i++) {
				for (int j=0; j<ylen; j++) {
					for (int k=0; k<zlen; k++) {
						if (dists[i][j][k] < 0) {
							section.setId(i, j, k, id);
						}
					}
				}
			}
		}
		
		public void apply(int[] ids) {
			Random rand = new Random();
			for (int i=0; i<xlen; i++) {
				for (int j=0; j<ylen; j++) {
					for (int k=0; k<zlen; k++) {
						if (dists[i][j][k] < 0) {
							section.setId(i, j, k, ids[rand.nextInt(ids.length)]);
						}
					}
				}
			}
		}
	}
}