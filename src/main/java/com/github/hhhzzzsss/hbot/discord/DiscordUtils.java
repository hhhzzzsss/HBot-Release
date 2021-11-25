package com.github.hhhzzzsss.hbot.discord;

import com.github.hhhzzzsss.hbot.command.Permission;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class DiscordUtils {
	public static int getMemberPermissionInt(Member member) {
		if (member.getGuild().getIdLong() != 810027180707938346L) {
			return 0;
		}
		Permission[] perms = Permission.values();
		int permVal = 0;
		for (Permission perm : perms) {
			for (Role rank : member.getRoles()) {
				if (rank.getName().equalsIgnoreCase(perm.discordRank())) {
					permVal = Math.max(permVal, perm.asInt());
				}
			}
		}
		return permVal;
	}
	
	public static Permission getMemberPermission(Member member) {
		if (member.getGuild().getIdLong() != 810027180707938346L) {
			return Permission.NONE;
		}
		Permission[] perms = Permission.values();
		int permVal = 0;
		for (Permission perm : perms) {
			for (Role rank : member.getRoles()) {
				if (rank.getName().equalsIgnoreCase(perm.discordRank())) {
					permVal = Math.max(permVal, perm.asInt());
				}
			}
		}
		return perms[permVal];
	}
	
	public static String sanitizeMentions(String message) {
		return message.replaceAll("@([^@])", "@​$1").replaceAll("([\\\\`*_~/])", "\\\\$1");
	}
}
