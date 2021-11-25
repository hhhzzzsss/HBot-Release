package com.github.hhhzzzsss.hbot.processes;

import java.util.Random;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;

public class EntitySpammer extends CoreProcess {
	
	HBot hbot;
	String entity;
	double x, y, z;
	double spread;
	String nbt;
	Random rand = new Random();
	
	public EntitySpammer(HBot hbot, String entity, double x, double y, double z, double spread, String nbt) {
		this.hbot = hbot;
		this.entity = entity;
		this.x = x;
		this.y = y;
		this.z = z;
		this.spread = spread;
		this.nbt = nbt;
	}
	
	@Override
	public void onTick() {
		int cornerX = Math.floorDiv((int)hbot.getPosManager().getX(), 16)*16;
    	int cornerZ = Math.floorDiv((int)hbot.getPosManager().getZ(), 16)*16;
		hbot.getCommandCore().run(String.format("minecraft:kill @e[type=!player,type=!%s,x=%d,y=-1,z=%d,dx=18,dy=258,dz=18]", entity, cornerX-1, cornerZ-1));
		hbot.getCommandCore().run(String.format("summon %s %f %f %f %s", entity, hbot.getPosManager().getX(), 300.0, hbot.getPosManager().getZ(), nbt));
		double dx = rand.nextGaussian()*spread;
		double dy = rand.nextGaussian()*spread;
		double dz = rand.nextGaussian()*spread;
		hbot.getCommandCore().run(String.format("data merge entity @e[type=%s,x=%d,y=-1,z=%d,dx=18,dy=350,dz=18,limit=1] {Pos:[%fd,%fd,%fd]}", entity, cornerX-1, cornerZ-1, x+dx, y+dy, z+dz));
		hbot.getCommandCore().run(String.format("data merge entity @e[type=%s,x=%d,y=-1,z=%d,dx=18,dy=350,dz=18,limit=1] {Pos:[%fd,%fd,%fd]}", entity, cornerX-1, cornerZ-1, x+dx, y+dy, z+dz));
	}
}
