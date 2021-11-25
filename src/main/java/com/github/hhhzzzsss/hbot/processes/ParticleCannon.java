package com.github.hhhzzzsss.hbot.processes;

import java.util.ArrayList;
import com.github.hhhzzzsss.hbot.entity.LivingEntity;
import com.github.hhhzzzsss.hbot.modules.PositionManager;
import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.entity.Entity;
import com.github.hhhzzzsss.hbot.entity.EntitySelector;
import com.github.hhhzzzsss.hbot.entity.EntityTracker;

public class ParticleCannon extends CoreProcess {
	public static final double eyeHeight = 1.6;
	
	public final HBot hbot;
	
	EntityTracker mobTracker;
	PositionManager posManager;
	EntitySelector mobType;
	String particleType;
	double speed;
	int maxTargets;
	boolean exactDistance;
	
	public ParticleCannon(HBot hbot, EntitySelector mobType, String particleType, double speed, int maxTargets) {
		this.hbot = hbot;
		this.mobTracker = hbot.getEntityTracker();
		this.posManager = hbot.getPosManager();
		this.mobType = mobType;
		this.particleType = particleType;
		this.speed = speed;
		this.maxTargets = maxTargets;
		if (particleType.equalsIgnoreCase("portal") || particleType.equalsIgnoreCase("enchant")) {
			exactDistance = true;
		}
	}
	
	@Override
	public void onTick() {
		for (Entity entity : getNearestEntities()) {
			double dx = entity.getX() - posManager.getX();
			double dy = entity.getY()+entity.getHeight()/2.0 - (posManager.getY()+eyeHeight);
			double dz = entity.getZ() - posManager.getZ();
			double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
			dx /= d;
			dy /= d;
			dz /= d;
			hbot.getCommandCore().run(String.format("particle %s %f %f %f %f %f %f %f 0 force", particleType, posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), dx, dy, dz, exactDistance ? d : speed));
		}
	}
	
	private Entity[] getNearestEntities() {
		ArrayList<Entity> entityArr = new ArrayList<>();
		for (Entity entity : mobTracker.getEntityMap().values()) if (mobType.matches(entity.getType()) && entityIsTargetable(entity)) {
			entityArr.add(entity);
		}
		entityArr.sort( (a, b) -> {
			double dax = posManager.getX() - a.getX();
			double day = posManager.getY()+eyeHeight - (a.getY()+a.getHeight()/2.0);
			double daz = posManager.getZ() - a.getZ();
			double dbx = posManager.getX() - b.getX();
			double dby = posManager.getY()+eyeHeight - (b.getY()+b.getHeight()/2.0);
			double dbz = posManager.getZ() - b.getZ();

			double da = dax*dax + day*day + daz*daz;
			double db = dbx*dbx + dby*dby + dbz*dbz;
			
			if (da<db) {
				return -1;
			}
			else if (da>db) {
				return 1;
			}
			else {
				return 0;
			}
		});
		
		if (entityArr.size() <= maxTargets) {
			return entityArr.toArray(new Entity[0]);
		}
		else {
			return entityArr.subList(0, maxTargets).toArray(new Entity[0]);
		}
	}
	
	private boolean entityIsTargetable(Entity entity) {
		if (entity.isDeleted()) {
			return false;
		}
		else if (entity instanceof LivingEntity) {
			return ((LivingEntity) entity).isAlive();
		}
		else {
			return true;
		}
	}
}
