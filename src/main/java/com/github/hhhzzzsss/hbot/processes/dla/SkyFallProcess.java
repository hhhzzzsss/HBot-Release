package com.github.hhhzzzsss.hbot.processes.dla;

import java.util.Random;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.BlockSelector;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.util.Vec3i;

public class SkyFallProcess extends DLAProcess {
	private int iterations;
	
	public SkyFallProcess(HBot hbot, String[] materials, BlockSelector selector, int iterations) throws CommandException {
		super(hbot, materials, selector);
		this.iterations = iterations;
		
		loaderThread = new SkyFallLoaderThread();
		loaderThread.start();
	}
	
	public class SkyFallLoaderThread extends Thread {
		private Random rand = new Random();
		
		public void run() {
			try {
				for (int i=0; i<iterations; i++) {
					doParticle();
				}
			}
			catch (Exception e) {
				exception = e;
				e.printStackTrace();
			}
		}
		
		public void doParticle() {
			int px = rand.nextInt(blockLength);
			int pz = rand.nextInt(blockLength);
			int py = 255;
			
			while (py > 0) {
				if (blocks[py][pz][px] > 0) {
					if (blocks[py][pz][px] == 1) {
						buildSequence.add(new Vec3i(px, py, pz));
						setSticky(px, py, pz);
					}
					return;
				}
				
				py--;
				px += rand.nextInt(3) - 1;
				pz += rand.nextInt(3) - 1;
				if (px < 0) {
					px = 0;
				}
				else if (px == blockLength) {
					px = blockLength-1;
				}
				if (pz < 0) {
					pz = 0;
				}
				else if (pz == blockLength) {
					pz = blockLength-1;
				}
			}

			if (blocks[py][pz][px] <= 1) {
				buildSequence.add(new Vec3i(px, py, pz));
				setSticky(px, py, pz);
			}
		}
	}
}
