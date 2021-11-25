package com.github.hhhzzzsss.hbot.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.subBots.OrbitBot;

import lombok.Getter;

public class OrbitManager {
	@Getter protected List<OrbitBot> orbitBots = new ArrayList<>();
	
	public void createOrbit(Bot parent, String targetUsername, UUID targetUUID) throws CommandException {
		// remove dead bots
		Iterator<OrbitBot> itr = orbitBots.iterator();
		while (itr.hasNext()) {
			OrbitBot bot = itr.next();
			if (!bot.isRunning()) {
				itr.remove();
			}
		}
		
		if (orbitBots.size() >= 3) {
			throw new CommandException("The is a maximum of 3 orbit bots total");
		}
		
		for (OrbitBot orbitBot : orbitBots) {
			if (orbitBot.getTargetUsername().equals(targetUsername)) {
				throw new CommandException("You already have a bot orbiting you");
			}
		}
		
		OrbitBot orbitBot = new OrbitBot(parent, targetUsername, targetUUID);
		orbitBots.add(orbitBot);
		orbitBot.start();
	}
	
	public void removeOrbit(String targetUsername) throws CommandException {
		Iterator<OrbitBot> itr = orbitBots.iterator();
		while (itr.hasNext()) {
			OrbitBot orbitBot = itr.next();
			if (orbitBot.getTargetUsername().equals(targetUsername)) {
				orbitBot.stop();
				itr.remove();
				return;
			}
		}
		throw new CommandException("You don't have a bot orbiting you");
	}
}
