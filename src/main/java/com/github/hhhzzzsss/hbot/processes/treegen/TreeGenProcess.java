package com.github.hhhzzzsss.hbot.processes.treegen;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;

public class TreeGenProcess extends CoreProcess {
	private final HBot hbot;
	private final PlatformInfo platform;
	public int originX;
	public int originY;
	public int originZ;
	TreeType type;
	public int currentIndex = 0;
	public Section section;
	GeneratingThread thread;
	
	public TreeGenProcess(HBot hbot, PlatformInfo platform, int x, int y, int z, TreeType type, Iterable<String> flags) {
		this.hbot = hbot;
		this.platform = platform;
		this.originX = x;
		this.originY = y;
		this.originZ = z;
		this.type = type;
		thread = new GeneratingThread(type, flags, platform);
		thread.start();
	}
	
	@Override
	public void onSequence() {
		if (done) {
			return;
		}
		if (section == null) {
			if (thread.isAlive()) {
				return;
			}
			else {
				if (thread.done) {
					section = thread.section;
					section.setOrigin(originX, originY, originZ);
					section.cubicOptimize(256);
				}
				else {
					platform.sendMessage("&cError in tree generating thread");
					done = true;
					return;
				}
			}
		}
		
		String command = section.nextCommand();
		if (command == null) {
			done = true;
			platform.sendMessage("&2Finished building");
		}
		else {
			hbot.getCommandCore().run(command);
		}
	}
	
	@Override
	public void stop() {
		thread.interrupt();
		done = true;
	}
}
