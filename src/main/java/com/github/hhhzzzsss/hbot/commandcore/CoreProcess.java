package com.github.hhhzzzsss.hbot.commandcore;

import lombok.*;

@RequiredArgsConstructor
public abstract class CoreProcess {
	public void onTick() {}
	public void onPacket() {}
	public void onSequence() {};
	
	@Getter protected boolean done = false;
	
	public void stop() {
		done = true;
	}
}
