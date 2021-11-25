package com.github.hhhzzzsss.hbot.processes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;
import com.github.hhhzzzsss.hbot.entity.Entity;
import com.github.hhhzzzsss.hbot.entity.EntitySelector;
import com.github.hhhzzzsss.hbot.entity.LivingEntity;
import com.github.hhhzzzsss.hbot.modules.PositionManager;
import com.github.hhhzzzsss.hbot.util.HashUtils;
import com.github.hhhzzzsss.hbot.entity.EntityTracker;

public class Sentry extends CoreProcess {
	public static enum AttackType {
		ARROWS,
		GRAVITY_ARROWS,
		WITHER,
		EXORCISM,
		SNIPE,
		PENETRATE,
		AIRSTRIKE,
		ANNIHILATION_BEAM,
		MISSILE,
		CHAIN_LIGHTNING,
		FREEZE,
		POTIONS,
		GRAVITY_POTIONS,
		TRIDENTS,
	}
	
	public static final double eyeHeight = 1.6;
	
	Random rand = new Random();
	
	private final HBot hbot;
	private final CommandCore commandCore;
	private final PositionManager posManager;
	private final EntityTracker entityTracker;
	EntitySelector mobSelector;
	AttackType attackType;
	int maxTargets;
	
	int attackProgress = 0;
	boolean attackEnd = false;
	
	public Sentry(HBot hbot, EntitySelector mobType, AttackType attackType, int maxTargets) {
		this.hbot = hbot;
		this.commandCore = hbot.getCommandCore();
		this.posManager = hbot.getPosManager();
		this.entityTracker = hbot.getEntityTracker();
		this.mobSelector = mobType;
		this.attackType = attackType;
		this.maxTargets = maxTargets;
	}

	ArrayList<double[]> positionTargets = new ArrayList<>();
	ArrayList<Entity> entityTargets = new ArrayList<>();
	LinkedList<EntityChaser> chasers = new LinkedList<>();
	
	@Override
	public void onTick() {
		if (attackEnd) {
			attackProgress = 0;
			positionTargets.clear();
			entityTargets.clear();
			attackEnd = false;
		}
		if (attackType == AttackType.ARROWS) {
			arrowsTick();
		}
		else if (attackType == AttackType.GRAVITY_ARROWS) {
			gravityArrowsTick();
		}
		else if (attackType == AttackType.WITHER) {
			witherTick();
		}
		else if (attackType == AttackType.EXORCISM) {
			exorcismTick();
		}
		else if (attackType == AttackType.SNIPE) {
			snipeTick();
		}
		else if (attackType == AttackType.PENETRATE) {
			penetrateTick();
		}
		else if (attackType == AttackType.AIRSTRIKE) {
			airstrikeTick();
		}
		else if (attackType == AttackType.ANNIHILATION_BEAM) {
			annihilationBeamTick();
		}
		else if (attackType == AttackType.MISSILE) {
			missileTick();
		}
		else if (attackType == AttackType.CHAIN_LIGHTNING) {
			chainLightningTick();
		}
		else if (attackType == AttackType.FREEZE) {
			freezeTick();
		}
		else if (attackType == AttackType.POTIONS) {
			potionsTick();
		}
		else if (attackType == AttackType.GRAVITY_POTIONS) {
			gravityPotionsTick();
		}
		else if (attackType == AttackType.TRIDENTS) {
			tridentsTick();
		}
		attackProgress++;
	}
	
	private void arrowsTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle portal %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 35) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 50 && entities.length > 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle flame %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress >= 50 && attackProgress % 2 == 0) {
			if (entities.length > 0) commandCore.run(String.format("playsound entity.skeleton.shoot master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			String uuidTag = HashUtils.getUUIDTag(hbot.getUuid());
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ(), 5.0);

				commandCore.run(String.format("summon arrow %f %f %f {NoGravity:1b,%s,life:1200,PierceLevel:127b,damage:99999999d,crit:1b,Motion:[%f,%f,%f]}", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), uuidTag, dv[0], dv[1], dv[2]));
			}
		}
		if (attackProgress >= 69) {
			attackEnd = true;
		}
	}
	

	
	private void gravityArrowsTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			if (entities.length > 0) commandCore.run(String.format("playsound entity.skeleton.shoot master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			String uuidTag = HashUtils.getUUIDTag(hbot.getUuid());
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ());
				double h = Math.sqrt(dv[0]*dv[0] + dv[2]*dv[2]);
				double[] launch = tryLaunchVelocities(h, dv[1], 0.05, 0.01, 3, 5, 6);
				if (launch != null) {
					double v = launch[0];
					double pitch = launch[1];
					double vx = h==0 ? 0 : v * dv[0]/h * Math.cos(pitch);
					double vy =            v *           Math.sin(pitch);
					double vz = h==0 ? 0 : v * dv[2]/h * Math.cos(pitch);
					commandCore.run(String.format("summon arrow %f %f %f {%s,life:1200,damage:2d,crit:1b,Motion:[%f,%f,%f]}", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), uuidTag, vx, vy, vz));
				}
			}
		}
		if (attackProgress >= 3) {
			attackEnd = true;
		}
	}
	
	private void witherTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle portal %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 35) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 50 && entities.length > 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle large_smoke %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress >= 50) {
			if (entities.length > 0) commandCore.run(String.format("playsound entity.wither.shoot master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			for (Entity entity : entities) {
				double[] facing = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), posManager.getY()+eyeHeight, entity.getZ(), 1.2);
				double xOff = attackProgress % 2 == 0 ? facing[2] : -facing[2];
				double zOff = attackProgress % 2 == 0 ? -facing[0] : facing[0];
				double[] dv = getVectorToPoint(posManager.getX()+xOff, posManager.getY()+eyeHeight, posManager.getZ()+zOff, entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ(), 3.0);
				commandCore.run(String.format("summon wither_skull %f %f %f {direction:[%f,%f,%f],power:[%f,%f,%f]}", posManager.getX()+xOff, posManager.getY()+eyeHeight, posManager.getZ()+zOff, dv[0], dv[1], dv[2], dv[0]/30.0, dv[1]/30.0, dv[2]/30.0));
			}
		}
		if (attackProgress >= 69) {
			attackEnd = true;
		}
	}
	
	private void exorcismTick() {
		if (attackProgress == 0) {
			Entity[] entities = getNearestEntities();
			if (entities.length == 0) {
				attackEnd = true;
				return;
			}
			for (Entity entity : entities) {
				entityTargets.add(entity);
			}
		}
		if (attackProgress < 60) {
			drawStarCircle("flame", posManager.getX(), posManager.getY(), posManager.getZ(), 3.0, attackProgress / 10.0 % 1.0);
			for (Entity entity : entityTargets) {
				drawStarCircle("flame", entity.getX(), entity.getY(), entity.getZ(), 2.0, attackProgress / 8.0 % 1.0);
			}
		}
		if (attackProgress == 45) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 60) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle flame %f %f %f 2 0 2 0.2 100 force", posManager.getX(), posManager.getY(), posManager.getZ()));
			commandCore.run(String.format("playsound entity.wither.shoot master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress >= 60) {
			for (Entity entity : entityTargets) {
				for (int i=0; i<3; i++) {
					double t = 2.0*Math.PI*Math.random();
					double u = Math.random() + Math.random();
					double r = (u>1 ? 2-u : u) * 2;
					double xOff = r*Math.cos(t);
					double zOff = r*Math.sin(t);
					commandCore.run(String.format("summon small_fireball %f %f %f {direction:[0.0,-1.0,0.0],power:[0.0,-0.1,0.0]}", entity.getX()+xOff, entity.getY()+entity.getHeight()+2.0, entity.getZ()+zOff));
				}
				commandCore.run(String.format("playsound entity.blaze.shoot master @a %f %f %f 2", entity.getX(), entity.getY(), entity.getZ()));
			}
		}
		if (attackProgress == 100) {
			attackEnd = true;
		}
	}

	private void snipeTick() {
		if (attackProgress == 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle portal %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 35) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 50) {
			Entity[] entities = getNearestEntities();
			if (entities.length > 0) commandCore.run(String.format("playsound entity.skeleton.shoot master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ(), 10.0);
				if (dv[3] < 3.0) {
					continue;
				}
				for (int i=0; i<20; i++) {
					double a = 1.5*i / 20.0;
					commandCore.run(String.format("particle campfire_cosy_smoke %f %f %f 0.15 0.15 0.15 0.01 3 force", posManager.getX() + dv[0]*a, posManager.getY()+eyeHeight + dv[1]*a, posManager.getZ() + dv[2]*a));
				}
				commandCore.run(String.format("summon fireball %f %f %f {ExplosionPower:999,direction:[%f,%f,%f],power:[%f,%f,%f]}", posManager.getX()+dv[0]*3.0/10.0, posManager.getY()+eyeHeight+dv[1]*3.0/10.0, posManager.getZ()+dv[2]*3.0/10.0, dv[0], dv[1], dv[2], dv[0]/10.0, dv[1]/10.0, dv[2]/10.0));
			}
		}
		if (attackProgress >= 69) {
			attackEnd = true;
		}
	}
	
	private void penetrateTick() {
		if (attackProgress == 0) {
			for (int i=0; i<10; i++) commandCore.run(String.format("particle portal %f %f %f 0 0 0 2 100 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 35) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 50) {
			Entity[] entities = getNearestEntities();
			if (entities.length > 0) commandCore.run(String.format("playsound item.trident.return master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ(), 1.0);
				if (dv[3] < 3.0) {
					continue;
				}
				double particleD = Math.min(Math.max(dv[3]/3, 2), 8);
				double particleX = posManager.getX() + dv[0]*particleD;
				double particleY = posManager.getY()+eyeHeight + dv[1]*particleD;
				double particleZ = posManager.getZ() + dv[2]*particleD;
				for (int i=0; i<20; i++) {
					double angle = 2.0*Math.PI/20.0 * i;
					double linearSpeed = 0.02 * i;
					double[] circleAxes = getPerpendicularAxes(dv);
					double[] circVel = {circleAxes[0]*Math.cos(angle) + circleAxes[3]*Math.sin(angle), circleAxes[1]*Math.cos(angle) + circleAxes[4]*Math.sin(angle), circleAxes[2]*Math.cos(angle) + circleAxes[5]*Math.sin(angle)};
					commandCore.run(String.format("particle flame %f %f %f %f %f %f %f 0 force", particleX, particleY, particleZ, dv[0], dv[1], dv[2], linearSpeed));
					commandCore.run(String.format("particle flame %f %f %f %f %f %f %f 0 force", particleX, particleY, particleZ, circVel[0], circVel[1], circVel[2], 0.2));
				}
				commandCore.run(String.format("summon creeper %f %f %f {Invulnerable:1b,ExplosionRadius:127b,Fuse:0}", entity.getX(), entity.getY(), entity.getZ()));
			}
		}
		if (attackProgress >= 69) {
			attackEnd = true;
		}
	}
	
	private void airstrikeTick() {
		if (attackProgress == 0) {
			Entity[] entities = getNearestEntities();
			if (entities.length == 0) {
				attackEnd = true;
				return;
			}
			for (Entity entity : entities) {
				positionTargets.add(new double[] {entity.getX(), entity.getY(), entity.getZ()});
				for (int i=0; i<20; i++) {
					double angle = 2.0*Math.PI/20.0 * i;
					double linearSpeed = 0.02 * i;
					commandCore.run(String.format("particle flame %f %f %f 0 1 0 %f 0 force", entity.getX(), entity.getY(), entity.getZ(), linearSpeed));
					commandCore.run(String.format("particle flame %f %f %f %f %f %f %f 0 force", entity.getX(), entity.getY(), entity.getZ(),  Math.cos(angle), 0.0, Math.sin(angle), 0.2));
				}
			}
			commandCore.run(String.format("playsound block.anvil.land master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 10) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress == 20) {
			for (int i=0; i<20; i++) {
				double linearSpeed = 0.02 * i;
				commandCore.run(String.format("particle campfire_cosy_smoke %f %f %f 0 1 0 %f 0 force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), linearSpeed));
			}
			commandCore.run(String.format("playsound entity.firework_rocket.launch master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 30) {
			for (double[] pos : positionTargets) {
				commandCore.run(String.format("summon fireball %f %f %f {ExplosionPower:999,direction:[0.0,-3.0,0.0],power:[0.0,-0.2,0.0]}", pos[0], 255.0, pos[2]));
			}
		}
		if (attackProgress >= 150) {
			attackEnd = true;
		}
	}
	
	private void annihilationBeamTick() {
		if (attackProgress == 0) {
			Entity[] entities = getNearestEntities();
			for (Entity entity : entities) {
				double d = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY()+entity.getHeight()/2.0, entity.getZ(), 1.0)[3]; 
				if (d <= 100) {
					positionTargets.add(new double[] {entity.getX(), entity.getY()+entity.getHeight()/2.0, entity.getZ()});
				}
			}
			if (positionTargets.size() == 0) {
				attackEnd = true;
				return;
			}
		}
		if (true) {
			for (double[] pos : positionTargets) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), pos[0], pos[1], pos[2], 1.0);
				for (int i=0; i<10; i++) {
					double r = Math.random()*100.0;
					commandCore.run(String.format("particle dust 1 1 1 9 %f %f %f 0 0 0 0 1 force", posManager.getX()+dv[0]*r, posManager.getY()+eyeHeight+dv[1]*r, posManager.getZ()+dv[2]*r));
				}
				double[] c1 = {posManager.getX() + dv[0]*7.0, posManager.getY()+eyeHeight + dv[1]*7.0, posManager.getZ() + dv[2]*7.0};
				double[] c2 = {posManager.getX() + dv[0]*12.0, posManager.getY()+eyeHeight + dv[1]*12.0, posManager.getZ() + dv[2]*12.0};
				double[] circleAxes = getPerpendicularAxes(dv);
				double radius = 7.0;
				for (int i=0; i<5; i++) {
					double a1 = Math.random()*2.0*Math.PI;
					double a2 = Math.random()*2.0*Math.PI;
					double[] dCircle1 = {circleAxes[0]*Math.cos(a1) + circleAxes[3]*Math.sin(a1), circleAxes[1]*Math.cos(a1) + circleAxes[4]*Math.sin(a1), circleAxes[2]*Math.cos(a1) + circleAxes[5]*Math.sin(a1)};
					double[] dCircle2 = {circleAxes[0]*Math.cos(a2) + circleAxes[3]*Math.sin(a2), circleAxes[1]*Math.cos(a2) + circleAxes[4]*Math.sin(a2), circleAxes[2]*Math.cos(a2) + circleAxes[5]*Math.sin(a2)};
					if (attackProgress > 20.0) commandCore.run(String.format("particle dust 1 0.3 0 9 %f %f %f 0 0 0 0 1 force", c1[0] + dCircle1[0]*radius, c1[1] + dCircle1[1]*radius, c1[2] + dCircle1[2]*radius));
					if (attackProgress > 40.0) commandCore.run(String.format("particle dust 1 0.3 0 9 %f %f %f 0 0 0 0 1 force", c2[0] + dCircle2[0]*radius, c2[1] + dCircle2[1]*radius, c2[2] + dCircle2[2]*radius));
				}
			}
		}
		if (attackProgress <= 60 && attackProgress % 2 == 0) {
			double floatingpitch = Math.pow(2, (attackProgress-30) / 30.0);
			commandCore.run(String.format("playsound block.note_block.didgeridoo master @a %f %f %f 10 %f", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), floatingpitch));
		}
		if (attackProgress >= 60 && attackProgress < 100 && attackProgress % 2 == 0) {
			commandCore.run(String.format("playsound block.note_block.didgeridoo master @a %f %f %f 10 1.5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			commandCore.run(String.format("playsound block.note_block.didgeridoo master @a %f %f %f 10 2", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		if (attackProgress == 60) {
			commandCore.run("killall arrow " + getWorld());
		}
		if (attackProgress >= 100) {
			for (double[] pos : positionTargets) {
				for (int i=0; i<5; i++) {
					double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), pos[0], pos[1], pos[2], 1.0);
					double rd = Math.random()*100.0;
					double[] c = {posManager.getX() + dv[0]*rd, posManager.getY()+eyeHeight + dv[1]*rd, posManager.getZ() + dv[2]*rd};
					double[] rg = {rand.nextGaussian()*3.0, rand.nextGaussian()*3.0, rand.nextGaussian()*3.0};
					commandCore.run(String.format("summon tnt %f %f %f", c[0] + rg[0], c[1] + rg[1], c[2] + rg[2]));
				}
			}
		}
		if (attackProgress >= 250) {
			attackEnd = true;
		}
	}
	
	private void missileTick() {
		Iterator<EntityChaser> itr = chasers.iterator();
		while (itr.hasNext()) {
			EntityChaser chaser = itr.next();
			if (!entityIsTargetable(chaser.target)) {
				boolean chainSuccess = chaser.chain(50);
				if (!chainSuccess) {
					commandCore.run(String.format("particle large_smoke %f %f %f 0 0 0 0.05 100 force", chaser.x, chaser.y, chaser.z));
					commandCore.run(String.format("playsound block.fire.extinguish master @a %f %f %f 1", chaser.x, chaser.y, chaser.z));
					itr.remove();
					continue;
				}
			}
			if (chaser.hit) {
				commandCore.run(String.format("summon tnt %f %f %f", chaser.target.getX(), chaser.target.getY()+chaser.target.getHeight()/2.0, chaser.target.getZ()));
				System.out.println(String.format("summon tnt %f %f %f", chaser.target.getX(), chaser.target.getY()+chaser.target.getHeight()/2.0, chaser.target.getZ()));
				itr.remove();
			}
			else {
				commandCore.run(String.format("particle flame %f %f %f 0 0 0 0.05 100 force", chaser.x, chaser.y, chaser.z));
				chaser.accelerateTowardTarget();
				chaser.move();
			}
		}
		
		if (attackProgress == 0) {
			if (chasers.size() >= maxTargets*10) {
				attackEnd = true;
				return;
			}
			Entity[] entities = getRandomEntities();
			if (entities.length == 0) {
				attackEnd = true;
				return;
			}
			for (Entity entity : entities) {
				EntityChaser chaser = new EntityChaser(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian(), 0.2, 0.8, 1.0, entity, true);
				chasers.add(chaser);
			}
			commandCore.run(String.format("playsound entity.firework_rocket.launch master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		
		if (attackProgress >= 10) {
			attackEnd = true;
		}
	}
	
	private void chainLightningTick() {
		for (int i=0; i<5; i++) {
			Iterator<EntityChaser> itr = chasers.iterator();
			while (itr.hasNext()) {
				EntityChaser chaser = itr.next();
				if (!entityIsTargetable(chaser.target)) {
					boolean chainSuccess = chaser.chain(50);
					if (!chainSuccess) {
						commandCore.run(String.format("particle large_smoke %f %f %f 0 0 0 0.05 100 force", chaser.x, chaser.y, chaser.z));
						commandCore.run(String.format("playsound block.fire.extinguish master @a %f %f %f 1", chaser.x, chaser.y, chaser.z));
						itr.remove();
						continue;
					}
				}
				if (chaser.hit) {
					commandCore.run(String.format("summon lightning_bolt %f %f %f", chaser.target.getX(), chaser.target.getY(), chaser.target.getZ()));
					
					boolean chainSuccess = chaser.chain(30);
					if (!chainSuccess || chaser.chainCount > 20) {
						itr.remove();
						continue;
					}
				}
				else {
					commandCore.run(String.format("particle dust 0.5 0.9 1.0 1.0 %f %f %f 0.1 0.1 0.1 0 10 force", chaser.x, chaser.y, chaser.z));
					chaser.accelerateTowardTarget();
					chaser.move();
				}
			}
		}
		
		if (attackProgress == 0) {
			if (chasers.size() > 0) {
				attackEnd = true;
				return;
			}
			Entity[] entities = getNearestEntities();
			if (entities.length == 0) {
				attackEnd = true;
				return;
			}
			for (Entity entity : entities) {
				EntityChaser chaser = new EntityChaser(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian(), 0.2, 0.8, 1.0, entity, true);
				chasers.add(chaser);
			}
			commandCore.run(String.format("playsound item.trident.thunder master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		
		if (attackProgress >= 100) {
			attackEnd = true;
		}
	}
	
	private void freezeTick() {
		Iterator<EntityChaser> itr = chasers.iterator();
		while (itr.hasNext()) {
			EntityChaser chaser = itr.next();
			if (!entityIsTargetable(chaser.target)) {
				boolean chainSuccess = chaser.chain(50);
				if (!chainSuccess) {
					commandCore.run(String.format("particle large_smoke %f %f %f 0 0 0 0.05 100 force", chaser.x, chaser.y, chaser.z));
					commandCore.run(String.format("playsound block.fire.extinguish master @a %f %f %f 1", chaser.x, chaser.y, chaser.z));
					itr.remove();
					continue;
				}
			}
			if (chaser.hit) {
				if (chaser.attackProgress == 0) {
					chaser.x = chaser.target.getX();
					chaser.y = chaser.target.getY() + chaser.target.getHeight();
					chaser.z = chaser.target.getZ();
				}
				int cx = (int) Math.floor(chaser.x);
				int cy = (int) Math.floor(chaser.y);
				int cz = (int) Math.floor(chaser.z);
				if (chaser.attackProgress == 0) {
					commandCore.run(String.format("fill %d %d %d %d %d %d packed_ice replace", cx-2, cy-2, cz-2, cx+2, cy+2, cz+2));
					commandCore.run(String.format("playsound block.glass.break master @a %d %d %d 3", cx, cy, cz));
					chaser.attackProgress++;
				}
				else if (chaser.attackProgress <= 10) {
					int r1 = (rand.nextInt(3)+2) * (rand.nextBoolean() ? -1 : 1);
					int r2 = rand.nextInt(7)-3;
					int r3 = rand.nextInt(7)-3;
					int rax = rand.nextInt(3);
					if (rax == 0) {
						cx += r1;
						cy += r2;
						cz += r3;
					}
					else if (rax == 1) {
						cx += r2;
						cy += r1;
						cz += r3;
					}
					else {
						cx += r2;
						cy += r3;
						cz += r1;
					}
					commandCore.run(String.format("fill %d %d %d %d %d %d packed_ice replace", cx-1, cy-1, cz-1, cx+1, cy+1, cz+1));
					commandCore.run(String.format("playsound block.glass.break master @a %d %d %d 3", cx, cy, cz));
					chaser.attackProgress++;
				}
				else {
					//commandCore.run(String.format("summon tnt %f %f %f", chaser.target.getX(), chaser.target.getY()+chaser.target.getHeight()/2.0, chaser.target.getZ()));
					itr.remove();
				}
			}
			else {
				commandCore.run(String.format("particle dust 0.0 0.5 1.0 1.0 %f %f %f 0.1 0.1 0.1 0 10 force", chaser.x, chaser.y, chaser.z));
				chaser.accelerateTowardTarget();
				chaser.move();
			}
		}
		
		if (attackProgress < 20) {
			if (chasers.size() > 0 || getRandomEntities().length == 0) {
				attackEnd = true;
				return;
			}
			if (attackProgress == 0) {
				commandCore.run(String.format("playsound entity.illusioner.prepare_mirror master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			}
			commandCore.run(String.format("particle dust 0.0 0.5 1.0 1.0 %f %f %f 1.5 1.5 1.5 0 %d force", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), (attackProgress+1)*4));
		}
		
		if (attackProgress == 20) {
			if (chasers.size() > 0) {
				attackEnd = true;
				return;
			}
			Entity[] entities = getRandomEntities();
			if (entities.length == 0) {
				attackEnd = true;
				return;
			}
			for (Entity entity : entities) {
				EntityChaser chaser = new EntityChaser(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian(), 0.2, 0.8, 1.0, entity, true);
				chasers.add(chaser);
			}
			commandCore.run(String.format("playsound entity.illusioner.cast_spell master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
		}
		
		if (attackProgress >= 120) {
			attackEnd = true;
		}
	}
	
	public static final String[] potionTypes = {
		"empty",
		"water",
		"awkward",
		"mundane",
		"thick",
		"swiftness",
		"strong_swiftness",
		"long_swiftness",
		"slowness",
		"long_slowness",
		"strength",
		"strong_strength",
		"long_strength",
		"weakness",
		"long_weakness",
		"healing",
		"strong_healing",
		"harming",
		"strong_harming",
		"leaping",
		"strong_leaping",
		"long_leaping",
		"luck",
		"regeneration",
		"strong_regeneration",
		"long_regeneration",
		"poison",
		"strong_poison",
		"long_poison",
		"fire_resistance",
		"long_fire_resistance",
		"water_breathing",
		"long_water_breathing",
		"night_vision",
		"long_night_vision",
		//"invisibility",
		//"long_invisibility",
		"turtle_master",
		"strong_turtle_master",
		"long_turtle_master",
		"slow_falling",
		"long_slow_falling",
	};
	
	private void potionsTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			if (entities.length > 0) commandCore.run(String.format("playsound entity.splash_potion.throw master @a %f %f %f 5 0.5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ(), 2.0);
				String randomPotion = potionTypes[rand.nextInt(potionTypes.length)];
				int modulus = 3000;
				int color = Color.HSBtoRGB((System.currentTimeMillis() % modulus) / (float) modulus, 1, 1);
				commandCore.run(String.format("summon potion %f %f %f {NoGravity:1b,Motion:[%f,%f,%f],Item:{id:\"minecraft:splash_potion\",Count:1b,tag:{Potion:\"%s\",CustomPotionColor:%d}}}", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), dv[0], dv[1], dv[2], randomPotion, color));
			}
		}
		if (attackProgress >= 3) {
			attackEnd = true;
		}
	}
	
	private void gravityPotionsTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			if (entities.length > 0) commandCore.run(String.format("playsound entity.splash_potion.throw master @a %f %f %f 5 0.5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ());
				double h = Math.sqrt(dv[0]*dv[0] + dv[2]*dv[2]);
				double[] launch = tryLaunchVelocities(h, dv[1], 0.05, 0.01, 0.5, 1, 2, 3);
				if (launch != null) {
					double v = launch[0];
					double pitch = launch[1];
					double vx = h==0 ? 0 : v * dv[0]/h * Math.cos(pitch);
					double vy =            v *           Math.sin(pitch);
					double vz = h==0 ? 0 : v * dv[2]/h * Math.cos(pitch);
					String randomPotion = potionTypes[rand.nextInt(potionTypes.length)];
					int modulus = 3000;
					int color = Color.HSBtoRGB((System.currentTimeMillis() % modulus) / (float) modulus, 1, 1);
					commandCore.run(String.format("summon potion %f %f %f {Motion:[%f,%f,%f],Item:{id:\"minecraft:splash_potion\",Count:1b,tag:{Potion:\"%s\",CustomPotionColor:%d}}}", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), vx, vy, vz, randomPotion, color));
				}
			}
		}
		if (attackProgress >= 3) {
			attackEnd = true;
		}
	}
	
	private void tridentsTick() {
		Entity[] entities = getNearestEntities();
		if (attackProgress == 0) {
			if (entities.length > 0) commandCore.run(String.format("playsound item.trident.throw master @a %f %f %f 5", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ()));
			String uuidTag = HashUtils.getUUIDTag(hbot.getUuid());
			for (Entity entity : entities) {
				double[] dv = getVectorToPoint(posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), entity.getX(), entity.getY() + entity.getHeight()/2.0, entity.getZ());
				double h = Math.sqrt(dv[0]*dv[0] + dv[2]*dv[2]);
				double[] launch = tryLaunchVelocities(h, dv[1], 0.05, 0.01, 3, 5, 6);
				if (launch != null) {
					double v = launch[0];
					double pitch = launch[1];
					double vx = h==0 ? 0 : v * dv[0]/h * Math.cos(pitch);
					double vy =            v *           Math.sin(pitch);
					double vz = h==0 ? 0 : v * dv[2]/h * Math.cos(pitch);
					commandCore.run(String.format("summon trident %f %f %f {%s,Motion:[%f,%f,%f],Trident:{id:\"minecraft:trident\",Count:1b,tag:{Enchantments:[{id:\"minecraft:loyalty\",lvl:10s},{id:\"minecraft:channeling\",lvl:1s}]}}}", posManager.getX(), posManager.getY()+eyeHeight, posManager.getZ(), uuidTag, vx, vy, vz));
				}
			}
		}
		if (attackProgress >= 5) {
			attackEnd = true;
		}
	}
	
	private class EntityChaser {
		public double x, y, z;
		public double vx, vy, vz;
		public double accel;
		public double resistanceFactor;
		public double hitRadius;
		public Entity target;
		public boolean targetCenter;
		public boolean hit = false;
		public double[] entityDirection = {0,0,0,0};
		public int attackProgress = 0;
		public TreeSet<Entity> prevTargets = new TreeSet<>();
		public int chainCount =  0;
		
		public EntityChaser(double x, double y, double z, double vx, double vy, double vz, double accel, double resistanceFactor, double hitRadius, Entity target, boolean targetCenter) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.vx = vx;
			this.vy = vy;
			this.vz = vz;
			this.accel = accel;
			this.resistanceFactor = resistanceFactor;
			this.hitRadius = hitRadius;
			this.target = target;
			this.targetCenter = targetCenter;
		}
		
		public void addVel(double x, double y, double z) {
			this.vx += x;
			this.vy += y;
			this.vz += z;
		}
		
		public void accelerateTowardTarget() {
			double[] dv = getVectorToPoint(x, y, z, target.getX(), targetCenter ? target.getY()+target.getHeight()/2.0 : target.getY(), target.getZ(), accel);
			addVel(dv[0], dv[1], dv[2]);
		}
		
		public void move() {
			vx *= resistanceFactor;
			vy *= resistanceFactor;
			vz *= resistanceFactor;
			
			
			
			x += vx;
			y += vy;
			z += vz;
			entityDirection = getVectorToPoint(x, y, z, target.getX(), targetCenter ? target.getY()+target.getHeight()/2.0 : target.getY(), target.getZ(), 1.0);
			if (entityDirection[3] < hitRadius) {
				hit = true;
			}
		}
	
		public boolean chain(double minDist) {
			prevTargets.add(target);
			
			Entity nearest = null;
			for (Entity entity : entityTracker.getEntityMap().values()) if (mobSelector.matches(entity.getType()) && entityIsTargetable(entity) && !prevTargets.contains(entity)) {
				double dx = x - entity.getX();
				double dy = y - entity.getY();
				double dz = z - entity.getZ();
				double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
				if (d < minDist) {
					minDist = d;
					nearest = entity;
				}
			}
			
			if (nearest == null) {
				return false;
			}
			else {
				target = nearest;
				hit = false;
				chainCount++;
				return true;
			}
		}
		
		public boolean chain() {
			return chain(Double.POSITIVE_INFINITY);
		}
	}
	
	public static double[] starX = {Math.cos(0*Math.PI/5), Math.cos(4*Math.PI/5), Math.cos(8*Math.PI/5), Math.cos(2*Math.PI/5), Math.cos(6*Math.PI/5)};
	public static double[] starZ = {Math.sin(0*Math.PI/5), Math.sin(4*Math.PI/5), Math.sin(8*Math.PI/5), Math.sin(2*Math.PI/5), Math.sin(6*Math.PI/5)};
	private void drawStarCircle(String particle, double x, double y, double z, double r, double a) {
		for (int i=0; i<5; i++) {
			int j = (i + 1) % 5;
			double circleX = Math.cos(2.0*(a+i)*Math.PI/5.0);
			double circleZ = Math.sin(2.0*(a+i)*Math.PI/5.0);
			double lineX = (starX[j] - starX[i]) * a + starX[i];
			double lineZ = (starZ[j] - starZ[i]) * a + starZ[i];
			commandCore.run(String.format("particle %s %f %f %f 0 0 0 0 1 force", particle, x+circleX*r, y, z+circleZ*r));
			commandCore.run(String.format("particle %s %f %f %f 0 0 0 0 1 force", particle, x+lineX*r, y, z+lineZ*r));
		}
	}
	
	private double[] getPerpendicularAxes(double[] vec) {
		//normalize input vector
		double[] normal = new double[3];
		double dn = Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
		normal[0] = vec[0] / dn;
		normal[1] = vec[1] / dn;
		normal[2] = vec[2] / dn;
		
		// determine axis of reference vector
		double t_max = -1.0;
		int refDim = 0;
		for (int i=0; i<3; i++) {
			if (Math.abs(normal[i]) > t_max) {
				t_max = Math.abs(normal[i]);
				refDim = i;
			}
		}
		refDim = (refDim+1) % 3;
		
		// subtract dot product times normal from reference vector
		double[] vRes = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		vRes[refDim] = 1.0;
		vRes[0] -= normal[refDim] * normal[0];
		vRes[1] -= normal[refDim] * normal[1];
		vRes[2] -= normal[refDim] * normal[2];
		
		// normalize
		double dv = Math.sqrt(vRes[0]*vRes[0] + vRes[1]*vRes[1] + vRes[2]*vRes[2]);
		vRes[0] /= dv;
		vRes[1] /= dv;
		vRes[2] /= dv;
		
		// cross product
		vRes[3] = normal[1]*vRes[2] - normal[2]*vRes[1];
		vRes[4] = normal[2]*vRes[0] - normal[0]*vRes[2];
		vRes[5] = normal[0]*vRes[1] - normal[1]*vRes[0];
		
		return vRes;
	}
	
	public Entity[] getNearestEntities() {
		ArrayList<Entity> entityArr = new ArrayList<>();
		for (Entity entity : entityTracker.getEntityMap().values()) if (mobSelector.matches(entity.getType()) && entityIsTargetable(entity)) {
			entityArr.add(entity);
		}
		entityArr.sort( (a, b) -> {
			double dax = posManager.getX() - a.getX();
			double day = posManager.getY()+eyeHeight - a.getY();
			double daz = posManager.getZ() - a.getZ();
			double dbx = posManager.getX() - b.getX();
			double dby = posManager.getY()+eyeHeight - b.getY();
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
	
	public Entity[] getRandomEntities() {
		ArrayList<Entity> entityArr = new ArrayList<>();
		for (Entity entity : entityTracker.getEntityMap().values()) if (mobSelector.matches(entity.getType()) && entityIsTargetable(entity)) {
			entityArr.add(entity);
		}
		Collections.shuffle(entityArr);
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
	
	private String getWorld() {
		String dimension = hbot.getStateManager().getDimension();
		if (dimension.equals("minecraft:overworld")) {
			return "world";
		}
		else if (dimension.equals("minecraft:the_nether")) {
			return "world_the_nether";
		}
		else if (dimension.equals("minecraft:the_end")) {
			return "world_the_end";
		}
		else {
			return "world";
		}
	}
	
	private double[] getVectorToPoint(double x1, double y1, double z1, double x2, double y2, double z2, double size) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
		dx = dx * size / d;
		dy = dy * size / d;
		dz = dz * size / d;
		return new double[] {dx, dy, dz, d};
	}
	
	private double[] getVectorToPoint(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
		return new double[] {dx, dy, dz, d};
	}
	
	public static Double getNeededAngle(double tx, double ty, double g, double d, double v) {
		if (tx < ty * 0.001) { // If it's near the asymptotes, just return a vertical angle
			return ty>0 ? Math.PI/2.0 : -Math.PI/2.0;
		}
		
		double md = 1.0-d;
		double log_md = Math.log(md);
		double g_d = g/d; // This is terminal velocity
		double theta = Math.atan2(ty, tx);
		double prev_abs_ydif = Double.POSITIVE_INFINITY;
		
		// 20 iterations max, although it usually converges in 3 iterations
		for (int i=0; i<20; i++) {
			double cost = Math.cos(theta);
			double sint = Math.sin(theta);
			double tant = sint/cost;
			double vx = v * cost;
			double vy = v * sint;
			double y = tx*(g_d+vy)/vx - g_d*Math.log(1-d*tx/vx)/log_md;
			double ydif = y-ty;
			double abs_ydif = Math.abs(ydif);
			
			// If it's getting farther away, there's probably no solution
			if (abs_ydif>prev_abs_ydif) {
				return null;
			}
			else if (abs_ydif < 0.0001) {
				return theta;
			}
			
			double dy_dtheta = tx + g*tx*tant / ((-d*tx+v*cost)*log_md) + g*tx*tant/(d*v*cost) + tx*tant*tant;
			theta -= ydif/dy_dtheta;
			prev_abs_ydif = abs_ydif;
		}
		
		// If exceeded max iterations, return null
		return null;
	}
	
	/**
	 * From a list of launch velocities, finds the minimum launch velocity and angle that will hit the target.
	 * 
	 * @param tx
	 * @param ty
	 * @param g
	 * @param d
	 * @param velocities
	 * @return An array of double in which the first element is the velocity and the second element is angle
	 */
	public static double[] tryLaunchVelocities(double tx, double ty, double g, double d, double... velocities) {
		for (double v : velocities) {
			Double angle = getNeededAngle(tx, ty, g, d, v); 
			if (angle != null) {
				return new double[] {v, angle};
			}
		}
		return null;
	}
}
