package com.github.hhhzzzsss.hbot.processes.treegen;

import java.util.ArrayList;

import com.github.hhhzzzsss.hbot.util.Vec3d;

public class EnvelopeGenerator {
	public static ArrayList<AttractorNode> generateOak(Vec3d origin, int size, int num) {
		ArrayList<AttractorNode> points = new ArrayList<>();
		while (num > 0) {
			double nx = Math.random()-0.5;
			double ny = Math.random();
			double nz = Math.random()-0.5;
			double nd = nx*nx + nz*nz;
			double ty = (Math.pow(2.0-ny, 3.0) - 1.0) / 7.0;
			double nbound = 0.5 * Math.sqrt(1-Math.pow(2*ty-1, 2));
			double nr = nx*nx+4*ny*ny+nz*nz;
			if (nd < nbound*nbound && nr > 0.3*0.3) {
				Vec3d pos = origin.add( (new Vec3d(nx, ny, nz)).multiply(size) );
				points.add(new AttractorNode(pos));
				num--;
			}
		}
		return points;
	}
	
	public static ArrayList<AttractorNode> generateRoot(Vec3d origin, int size, int num) {
		ArrayList<AttractorNode> points = new ArrayList<>();
		while (num > 0) {
			double nx = Math.random()-0.5;
			double ny = 0;
			double nz = Math.random()-0.5;
			double nd = nx*nx + nz*nz;
			if (nd < 1) {
				Vec3d pos = origin.add( (new Vec3d(nx, ny, nz)).multiply(size) );
				points.add(new AttractorNode(pos));
				num--;
			}
		}
		return points;
	}
	
	public static ArrayList<AttractorNode> generateSphereSurface(Vec3d origin, int size, int num) {
		ArrayList<AttractorNode> points = new ArrayList<>();
		for (int i=0; i<num; i++) {
			double theta = 2.0 * Math.PI * Math.random();
			double phi = Math.acos(1.0 - 2.0 * Math.random());
			double nx = Math.sin(phi) * Math.cos(theta);
			double ny = Math.sin(phi) * Math.sin(theta);
			double nz = Math.cos(phi);
			Vec3d pos = origin.add( (new Vec3d(nx, ny, nz)).multiply(size).multiply(0.5) );
			points.add(new AttractorNode(pos));
		}
		return points;
	}
	
	public static ArrayList<AttractorNode> generateSphere(Vec3d origin, int size, int num) {
		ArrayList<AttractorNode> points = new ArrayList<>();
		while (num > 0) {
			double nx = Math.random()-0.5;
			double ny = Math.random()-0.5;
			double nz = Math.random()-0.5;
			double nr = nx*nx+ny*ny+nz*nz;
			if (nr < 0.25) {
				Vec3d pos = origin.add( (new Vec3d(nx, ny, nz)).multiply(size) );
				points.add(new AttractorNode(pos));
				num--;
			}
		}
		return points;
	}
	
	public static ArrayList<AttractorNode> generateBonsai(Vec3d origin, int vsize, int hsize, int numClumps, int perClump) {
		ArrayList<AttractorNode> points = new ArrayList<>();
		for (int clump = 0; clump < numClumps; clump++) {
			double cd = Math.sqrt(0.3 + 0.7 * Math.random())/2.0;
			double ctheta = clump * 2.3999632297;
			double cy = (clump+1.0) / numClumps;
			double cx = cd * Math.cos(ctheta);
			double cz = cd * Math.sin(ctheta);
			int num = perClump;
			while (num > 0) {
				double nx = Math.random()-0.5;
				double ny = Math.random()-0.5;
				double nz = Math.random()-0.5;
				ny /= 3.0;
				if (nx*nx + 9.0*ny*ny + nz*nz < 0.25) {
					Vec3d pos = origin.add( (new Vec3d(cx, cy, cz)).multiply(hsize, vsize, hsize) ).add( (new Vec3d(nx, ny, nz)).multiply(hsize*2.0/3.0) );
					points.add(new AttractorNode(pos));
					num--;
				}
			}
		}
		return points;
	}
}
