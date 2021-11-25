package com.github.hhhzzzsss.hbot.processes;

import java.util.Random;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;

public class PCrashProcess extends CoreProcess {
	
	HBot hbot;
	double x, y, z;
	
	public PCrashProcess(HBot hbot, double x, double y, double z) {
		this.hbot = hbot;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void onTick() {
		hbot.getCommandCore().run(String.format("particle minecraft:dust 1 0 0 9 %f %f %f 0 0 0 0 2147483646 force", x, y, z));
	}
}
