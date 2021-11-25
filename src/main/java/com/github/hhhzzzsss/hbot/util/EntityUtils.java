package com.github.hhhzzzsss.hbot.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.*;

public class EntityUtils {
	
	@Data
	public static class EntityData {
		private final int id;
		private final int internalId;
		private final String displayName;
		private final String name;
		private final double width;
		private final double height;
		private final String type;
	}
	
	private static EntityData[] entities;
	private static HashMap<String, EntityData> entitiesByName = new HashMap<>();
	static {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("entities.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		JsonArray blockJsonArray = JsonParser.parseReader(reader).getAsJsonArray();
		
		entities = new EntityData[blockJsonArray.size()];
		for (JsonElement blockElem : blockJsonArray) {
			JsonObject entityObj = blockElem.getAsJsonObject();
			EntityData entityData = new EntityData(
					entityObj.get("id").getAsInt(),
					entityObj.get("internalId").getAsInt(),
					entityObj.get("displayName").getAsString(),
					entityObj.get("name").getAsString(),
					entityObj.get("width").getAsDouble(),
					entityObj.get("height").getAsDouble(),
					entityObj.get("type").getAsString());
			entities[entityData.id] = entityData;
		}
		
		for (EntityData entity : entities) {
			entitiesByName.put(entity.name, entity);
		}
	}
	
	public static EntityData getEntity(int i) {
		return entities[i];
	}
	
	public static EntityData getEntityByName(String name) {
		return entitiesByName.get(name);
	}
}
