package com.github.hhhzzzsss.hbot;

import java.util.ArrayList;

public class Main {
	public static ArrayList<HBot> hbots = new ArrayList<>();

	public static void main(String[] args) {
		for (Config.BotInfo botInfo : Config.getConfig().getBots()) {
			HBot hbot = new HBot(botInfo.getHost(), botInfo.getPort(), botInfo.getServerNick(), botInfo.getDiscordToken(), botInfo.getCategoryName());
			hbots.add(hbot);
		}
		for (HBot hbot : hbots) {
			hbot.start();
		}
	}
	
	public static void restart() {
		for (HBot hbot : hbots) {
			hbot.getDiscordManager().shutdown();
			Logger.shutdownAndAwaitTermination();
			Bot.stopAllBotsAndAwaitTermination();
		}
		Logger.shutdownAndAwaitTermination();
		Bot.stopAllBotsAndAwaitTermination();
		for (int i=0; i<100; i++) {
			boolean discordFinished = true;
			for (HBot hbot : hbots) {
				if (!hbot.getDiscordManager().finished) {
					discordFinished = false;
				}
			}
			if (!discordFinished) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				break;
			}
		}
		System.exit(0);
	}
	
	public static void stop() {
		for (HBot hbot : hbots) {
			hbot.getDiscordManager().shutdown();
			Logger.shutdownAndAwaitTermination();
			Bot.stopAllBotsAndAwaitTermination();
		}
		Logger.shutdownAndAwaitTermination();
		Bot.stopAllBotsAndAwaitTermination();
		for (int i=0; i<100; i++) {
			boolean discordFinished = true;
			for (HBot hbot : hbots) {
				if (!hbot.getDiscordManager().finished) {
					discordFinished = false;
				}
			}
			if (!discordFinished) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				break;
			}
		}
		System.exit(42);
	}
}
