package com.github.hhhzzzsss.hbot.processes.boxstructure;

public class ContainerItem {
	public String name;
	public int count;
	public int slot;
	public String tag;
	
	public ContainerItem(int slot, String name, int count) {
		this.slot = slot;
		this.name = name;
		this.count = count;
	}
	
	public ContainerItem(int slot, String name, int count, String tag) {
		this.slot = slot;
		this.name = name;
		this.count = count;
		this.tag = tag;
	}
	
	public ContainerItem(int slot, Item item, int count) {
		this.slot = slot;
		this.name = item.name;
		this.count = count;
		if (item.tag != null) this.tag = item.tag;
	}
	
	public String toString() {
		if (tag == null) {
			return String.format("{Slot:%db,id:\"%s\",Count:%db}", slot, name, count);
		}
		else {
			return String.format("{Slot:%db,id:\"%s\",Count:%db,tag:%s}", slot, name, count, tag);
		}
	}
}
