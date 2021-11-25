package com.github.hhhzzzsss.hbot.processes.dla;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.BlockSelector;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.util.Vec3i;

import lombok.Getter;

public class BrownianProcess extends DLAProcess {
	private double particleRatio;
	
	public BrownianProcess(HBot hbot, String[] materials, BlockSelector selector, double particleRatio) throws CommandException {
		super(hbot, materials, selector);
		this.particleRatio = particleRatio;
		
		loaderThread = new BrownianLoaderThread();
		loaderThread.start();
	}
	
	public class Particle {
		public int x;
		public int y;
		public int z;
		public Particle(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public class BrownianLoaderThread extends Thread {
		private Random rand = new Random();

		private BitSet validSet = new BitSet(256*blockLength*blockLength);
		private ArrayList<Particle> particles = new ArrayList<>();
		private int iterationsWithoutSticking = 0;
		
		private long nextProgressMessage = System.currentTimeMillis() + 5000;
		private int totalParticles;
		
		public void run() {
			try {
				for (int x=0; x<blockLength; x++) for (int y=0; y<256; y++) for (int z=0; z<blockLength; z++) {
					if (blocks[y][x][z] == 1) {
						fillValidSet(x, y, z);
					}
				}
				
				for (int x=0; x<blockLength; x++) for (int y=0; y<256; y++) for (int z=0; z<blockLength; z++) {
					if (validSet.get(y*blockLength*blockLength + z*blockLength + x)) {
						if (rand.nextDouble() < particleRatio) {
							particles.add(new Particle(x, y, z));
						}
					}
				}
				
				totalParticles = particles.size();
				
				while (iterationsWithoutSticking < 10000) {
					Iterator<Particle> itr = particles.iterator();
					while (itr.hasNext()) {
						Particle particle = itr.next();
						
						int block = blocks[particle.y][particle.z][particle.x];
						if (block > 0) {
							if (block == 1) {
								buildSequence.add(new Vec3i(particle.x, particle.y, particle.z));
								setSticky(particle.x, particle.y, particle.z);
								iterationsWithoutSticking = 0;
							}
							itr.remove();
							continue;
						}
						
						int newX = particle.x + rand.nextInt(3)-1;
						int newY = particle.y + rand.nextInt(3)-1;
						int newZ = particle.z + rand.nextInt(3)-1;
						if (newX < 0 || newX == blockLength) newX = particle.x;
						if (newZ < 0 || newZ == blockLength) newZ = particle.z; 
						if (newY < 0 || newY == 256) newY = particle.y;
						
						block = blocks[newY][newZ][newX];
						if (block == 0) {
							particle.x = newX;
							particle.y = newY;
							particle.z = newZ;
						}
						else if (block == 1) {
							buildSequence.add(new Vec3i(newX, newY, newZ));
							setSticky(newX, newY, newZ);
							iterationsWithoutSticking = 0;
							itr.remove();
							continue;
						}
						// else don't do anything since it's an invalid move position
					}
					iterationsWithoutSticking++;
					
					if (System.currentTimeMillis() >= nextProgressMessage) {
						hbot.sendChatAsync(String.format("&7Particles attached: &3%d/%d", totalParticles - particles.size(), totalParticles));
						nextProgressMessage = System.currentTimeMillis() + 5000;
					}
				}
			}
			catch (Exception e) {
				exception = e;
				e.printStackTrace();
			}
		}
		
		private void fillValidSet(int xinit, int yinit, int zinit) {
			Queue<Vec3i> posQueue = new ArrayDeque<>();
			
			posQueue.add(new Vec3i(xinit, yinit, zinit));
			while (!posQueue.isEmpty()) {
				Vec3i pos = posQueue.poll();
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				
				if (x<0 || x>=blockLength || z<0 || z>=blockLength || y<0 || y>255) {
					continue;
				}
				int idx = y*blockLength*blockLength + z*blockLength + x;
				if (validSet.get(idx) || blocks[y][z][x] > 1) {
					continue;
				}
				
				validSet.set(idx, true);
				for (int i=-1; i<=1; i++) for (int j=-1; j<=1; j++) for (int k=-1; k<=1; k++) {
					posQueue.add(new Vec3i(x+i, y+j, z+k));
				}
			}
		}
	}
}
