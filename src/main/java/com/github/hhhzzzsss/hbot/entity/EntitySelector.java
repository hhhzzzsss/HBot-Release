package com.github.hhhzzzsss.hbot.entity;

import java.util.ArrayList;

import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;

public class EntitySelector {
	ArrayList<EntityType> exclude = new ArrayList<>();
	ArrayList<EntityType> include = new ArrayList<>();
	
	public EntitySelector(String selectorString) {
		String[] split = selectorString.split(",");
		for (String entityString : split) {
			if (entityString.startsWith("!")) {
				EntityType etype = EntityType.valueOf(entityString.substring(1).toUpperCase());
				exclude.add(etype);
			}
			else {
				EntityType etype = EntityType.valueOf(entityString.toUpperCase());
				include.add(etype);
			}
		}
	}
	
	public boolean matches(EntityType etype) {
		for (EntityType ex : exclude) {
			if (ex == etype) {
				return false;
			}
		}
		for (EntityType in : include) {
			if (in == etype) {
				return true;
			}
		}
		if (include.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
}
