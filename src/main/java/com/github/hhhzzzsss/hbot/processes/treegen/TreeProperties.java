package com.github.hhhzzzsss.hbot.processes.treegen;

public class TreeProperties {
	public boolean bare = false;
	public boolean hollow = false;
	public int size = 100;
	
	public static TreeProperties fromFlags(Iterable<String> flags) {
		TreeProperties properties = new TreeProperties();
		for (String flag : flags) {
			if (flag.equalsIgnoreCase("bare")) {
				properties.bare = true;
			}
			else if (flag.equalsIgnoreCase("hollow")) {
				properties.hollow = true;
			}
		}
		return properties;
	}
}
