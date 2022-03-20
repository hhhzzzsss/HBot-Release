package com.github.hhhzzzsss.hbot.processes.schem;

import java.io.File;
import java.io.IOException;

import com.github.hhhzzzsss.hbot.HBot;
import com.github.hhhzzzsss.hbot.block.Section;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.command.PlatformInfo;
import com.github.hhhzzzsss.hbot.commandcore.CommandCore;
import com.github.hhhzzzsss.hbot.commandcore.CoreProcess;

public class SchemProcess extends CoreProcess {
	private HBot hbot;
	private CommandCore commandCore;
	private PlatformInfo platform;
	Section section;

	SchemLoaderThread loaderThread;
	
	boolean loaded = false;
	
	public static final File SCHEM_DIR = new File("schematics");
	static {
		if (!SCHEM_DIR.exists()) {
			SCHEM_DIR.mkdir();
		}
	}

	public SchemProcess(HBot hbot, PlatformInfo platform, int x, int y, int z, String location, Iterable<String> flags) throws CommandException {
		this.hbot = hbot;
		commandCore = hbot.getCommandCore();
		this.platform = platform;

		try {
			loaderThread = new SchemLoaderThread(x, y, z, location, flags);
		} catch (IOException e) {
			throw new CommandException(e.getMessage());
		}
		loaderThread.start();
	}
	
	@Override
	public void onSequence() {
		if (done) return;

		if (!loaded) {
			if (!loaderThread.isAlive()) {
				if (loaderThread.getSection() != null) {
					section = loaderThread.getSection();
					platform.sendMessage("&7Now building &3" + loaderThread.getSchemName());
					loaded = true;
				} else if (loaderThread.getException() != null) {
					hbot.sendChat("&cError while loading schematic: &4" + loaderThread.getException().getMessage());
					done = true;
				}
			}
			return;
		}
		
		String command = section.nextCommand();
		if (command == null) {
			done = true;
			hbot.sendChat("&7Finished building");
		}
		else {
			commandCore.run(command);
		}
	}
}
