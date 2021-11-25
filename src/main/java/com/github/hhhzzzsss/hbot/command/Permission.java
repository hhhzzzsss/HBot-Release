package com.github.hhhzzzsss.hbot.command;

public enum Permission {
	NONE(0, "Everyone"),
	TRUSTED(1, "Trusted"),
	ADMIN(2, "Admin"),
	OWNER(3, "Owner");
	
	private int rank;
	private String discordRank;
	private Permission(int rank, String discordRank) {
		this.rank = rank;
		this.discordRank = discordRank;
	}
	public int asInt() {
		return rank;
	}
	public String discordRank() {
		return discordRank;
	}
}
