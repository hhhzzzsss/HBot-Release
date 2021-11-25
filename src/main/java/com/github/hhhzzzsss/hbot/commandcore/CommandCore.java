package com.github.hhhzzzsss.hbot.commandcore;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.block.ChunkColumn;
import com.github.hhhzzzsss.hbot.block.ChunkPos;
import com.github.hhhzzzsss.hbot.block.World;
import com.github.hhhzzzsss.hbot.command.CommandException;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.listeners.TickListener;
import com.github.hhhzzzsss.hbot.util.BlockUtils;
import com.github.steveice10.mc.protocol.data.game.chunk.BitStorage;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.palette.Palette;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.CommandBlockMode;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientUpdateCommandBlockPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.packet.Packet;

import lombok.*;
import net.kyori.adventure.text.TranslatableComponent;

@RequiredArgsConstructor
public class CommandCore implements TickListener, PacketListener {
	private final Bot bot;
	private final World world;
	
	private ChunkPos corePos = null;
	private ChunkPos relocatePos = null;
	@Getter private boolean coreLoaded = false;
	private int idx = 0;

	private int coreHeight = 3;
	@Getter private int targetCoreHeight = 3;
	@Getter @Setter private int commandSpeed = 10;
	@Getter private CoreProcess process = null;
	
	private int coreCheckCooldown = 20;
	
	@Override
	public void onTick() {
		if (bot.getPosManager().isSpawned() == false) {
			return;
		}
		
		if (coreCheckCooldown == 0) {
			coreCheckCooldown = 5; // normally delay 5 ticks between checks
			relocateIfNeeded();
			ChunkPos trackedPos = getTrackedPos();
			if (trackedPos != null) {
				if (checkCore(trackedPos)) {
					coreLoaded = true;
					coreHeight = targetCoreHeight;
					if (relocatePos != null) {
						if (corePos != null && !corePos.equals(relocatePos)) {
							deleteCore();
						}
						corePos = relocatePos;
						relocatePos = null;
					}
				}
				else {
					fillCore(trackedPos);
					coreCheckCooldown = 30; // delay 30 ticks if it's filling the core
				}
			}
		}
		coreCheckCooldown--;
		
		if (process != null) {
			if (!process.isDone()) process.onTick();
			if (commandLag < 127) {
				for (int i=0; i<commandSpeed; i++) {
					if (process.isDone()) {
						break;
					}
					process.onSequence();
				}
			}
			if (process.isDone()) {
				process = null;
			}
		}
	}
	
	@Getter @Setter private int commandLag = 0;
	public void onPacket(Packet packet) {
		if (packet instanceof ServerJoinGamePacket) {
			bot.sendCommand("/gamerule commandBlockOutput false");
		}
		else if (packet instanceof ServerChatPacket) {
			ServerChatPacket t_packet = (ServerChatPacket) packet;
			if (t_packet.getMessage() instanceof TranslatableComponent) {
				TranslatableComponent message = (TranslatableComponent) t_packet.getMessage();
				if (message.key().equals("advMode.setCommand.success") || message.key().equals("advMode.notEnabled") || message.key().equals("advMode.notAllowed")) {
					commandLag = Math.max(commandLag-2, 0);
				}
			}
		}
		
		if (process != null) {
			process.onPacket();
			if (process.isDone()) {
				process = null;
			}
		}
	}
	
	public void checkProcessFree() throws CommandException {
		if (this.process != null) {
			throw new CommandException("Another process is already running");
		}
	}
	
	public void setProcess(CoreProcess process) throws CommandException {
		checkProcessFree();
		this.process = process;
	}
	
	public void forceSetProcess(CoreProcess process) {
		if (this.process != null) this.process.stop();
		this.process = process;
	}
	
	private void relocateIfNeeded() {
		ChunkPos curChunk = getCurrentChunkPos();
		ChunkPos trackedPos = getTrackedPos();
		if (trackedPos == null) {
			relocatePos = curChunk;
		}
		else if (Math.abs(curChunk.getX() - trackedPos.getX()) > 5 || Math.abs(curChunk.getZ() - trackedPos.getZ()) > 5) {
			relocatePos = curChunk;
		}
	}
	
	private boolean checkCore(ChunkPos pos) {
		ChunkColumn chunkColumn = world.getChunk(pos);
		if (chunkColumn == null) return false;
		Chunk section = chunkColumn.getData()[0];
		if (section == null) return false;
		BitStorage storage = section.getStorage();
		Palette palette = section.getPalette();
		for (int i=0; i<256*targetCoreHeight; i++) {
			if (!isCommandBlock(palette.idToState(storage.get(i)))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if every block of the core is there and the core is currently in a chunk loaded by the bot.
	 * @return whether the core is complete
	 */
	public boolean isComplete() {
		return checkCore(corePos);
	}
	
	private void fillCore(ChunkPos pos) {
		int x = pos.getX() << 4;
		int z = pos.getZ() << 4;
		bot.sendCommand(String.format("/fill %d %d %d %d %d %d repeating_command_block replace", x, 0, z, x+15, targetCoreHeight-1, z+15));
	}
	
	private void deleteCore() {
		int x = corePos.getX() << 4;
		int z = corePos.getZ() << 4;
		run(String.format("/fill %d 0 %d %d 15 %d air replace repeating_command_block", x, z, x+15, z+15));
		idx = 0;
	}
	
	/**
	 * Minimum should be 1 and maximum should be 16.
	 * 
	 * @param newHeight The new core height.
	 */
	public void setCoreHeight(int newHeight) {
		if (newHeight > coreHeight) {
			coreCheckCooldown = 0;
			targetCoreHeight = newHeight;
		}
		else if (newHeight < coreHeight) {
			if (corePos != null) {
				int x = corePos.getX() << 4;
				int z = corePos.getZ() << 4;
				run(String.format("/fill %d %d %d %d %d %d air replace repeating_command_block", x, newHeight, z, x+15, 16, z+15));
				targetCoreHeight = newHeight;
				coreHeight = newHeight;
				idx++;
				if (idx >= 256*coreHeight) {
					idx = 0;
				}
			}
		}
	}
	
	public void run(String command) {
		if (corePos == null) {
			return;
		}
		int x = (corePos.getX() << 4) + (idx & 15);
		int z = (corePos.getZ() << 4) + ((idx>>4) & 15);
		int y = idx >> 8;
		Position p = new Position(x, y, z);
		bot.sendPacket(new ClientUpdateCommandBlockPacket(p, command, CommandBlockMode.AUTO, false, false, true));
		idx = (idx+1) % (256*coreHeight);
		commandLag++;
	}
	
	private ChunkPos getTrackedPos() {
		if (relocatePos == null) {
			return corePos;
		}
		else {
			return relocatePos;
		}
	}
	
	private ChunkPos getCurrentChunkPos() {
		int x = (int) Math.floor(bot.getPosManager().getX());
		int z = (int) Math.floor(bot.getPosManager().getZ());
		return new ChunkPos(x >> 4, z >> 4);
	}
	
	private int commandBlockId = BlockUtils.getBlockByName("command_block").getId();
	private int repeatingCommandBlockId = BlockUtils.getBlockByName("repeating_command_block").getId();
	private int chainCommandBlockId = BlockUtils.getBlockByName("chain_command_block").getId();
	private boolean isCommandBlock(int stateId) {
		int id = BlockUtils.getBlockByStateId(stateId).getId();
		return id == repeatingCommandBlockId;
	}
}
