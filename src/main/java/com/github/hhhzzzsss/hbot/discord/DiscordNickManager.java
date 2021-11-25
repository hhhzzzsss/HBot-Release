package com.github.hhhzzzsss.hbot.discord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

public class DiscordNickManager {
	private static Map<Long, String> nickMap = Collections.synchronizedMap(new HashMap<>());
    
    private static Gson gson = new Gson();
    private static final File discordnicksFile = new File("discordnicks.json");
    static {
    	readDiscordNicks();
    }
    
    public static void readDiscordNicks() {
    	if (discordnicksFile.exists()) {
    		InputStreamReader isReader;
    		try {
    			isReader = new InputStreamReader(new FileInputStream(discordnicksFile), "UTF-8");
     
    			JsonReader jsonReader = new JsonReader(isReader);
    			Type treeSetType = new TypeToken<HashMap<Long, String>>(){}.getType();
    			synchronized (nickMap) {
    				nickMap = gson.fromJson(jsonReader, treeSetType);
    			}
    			jsonReader.close();
     
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
	
	public static void writeDiscordNicks() {
		String json;
		synchronized(nickMap) {
	    	json = gson.toJson(nickMap);
		}
    	try {
			FileWriter fileWriter;
			fileWriter = new FileWriter(discordnicksFile);
			
			BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
			bufferWriter.write(json);
			bufferWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public static boolean hasNick(Member member) {
		readDiscordNicks();
		return nickMap.containsKey(member.getIdLong());
	}
	
	public static void setNick(Member member, String nick) {
		readDiscordNicks();
		nickMap.put(member.getIdLong(), nick);
		writeDiscordNicks();
	}
	
	public static void removeNick(Member member) {
		readDiscordNicks();
		nickMap.remove(member.getIdLong());
		writeDiscordNicks();
	}
	
	public static String getNick(Member member) {
		readDiscordNicks();
		String nick = nickMap.get(member.getIdLong());
		if (nick == null) {
			return member.getEffectiveName();
		}
		else {
			return nick;
		}
	}
}
