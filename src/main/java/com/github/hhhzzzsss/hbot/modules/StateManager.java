package com.github.hhhzzzsss.hbot.modules;

import com.github.hhhzzzsss.hbot.Bot;
import com.github.hhhzzzsss.hbot.listeners.DisconnectListener;
import com.github.hhhzzzsss.hbot.listeners.PacketListener;
import com.github.hhhzzzsss.hbot.listeners.TickListener;
import com.github.hhhzzzsss.hbot.util.ChatUtils;
import com.github.steveice10.mc.protocol.data.game.entity.EntityEvent;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.level.notify.GameEvent;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

@RequiredArgsConstructor
public class StateManager implements PacketListener, TickListener, DisconnectListener {
	private final Bot bot;
	
	@Getter @Setter public static long commandDelay = 1000; 
	private long nextRectifyTime = System.currentTimeMillis();
	
	@Getter @Setter private boolean autoOp = false;
	@Getter private boolean opped = true;
	
	@Getter @Setter private boolean autoUnmute = false;
	@Getter private boolean muted = false;
	
	@Getter @Setter private boolean autoVanish = false;
	@Getter @Setter private boolean targetVanish = true;
	@Getter private boolean vanish = false;
	
	@Getter @Setter private boolean autoGamemode = false;
	@Getter @Setter private GameMode targetGamemode = GameMode.CREATIVE;
	@Getter private GameMode gamemode = GameMode.CREATIVE;
	
	@Getter @Setter private boolean autoGodmode = false;
	@Getter private boolean godmode = false;
	
	@Getter @Setter private boolean autoCommandspy = false;
	@Getter private boolean commandspy = false;
	
	@Getter @Setter private boolean autoUsername = false;
	@Getter private boolean wrongUsername = false;
	
	@Getter @Setter private boolean autoNick = false;
	@Getter private boolean wrongNick = false;
	
	@Getter @Setter private boolean autoPrefix = false;
	@Getter private boolean wrongPrefix = false;
	
	@Getter private String dimension = "minecraft:overworld";
	@Getter private int entityId = -1;
	
	public static final Pattern mutePattern = Pattern.compile("You have been muted for (.*)\\.( Reason: .*)?");
	public static final Pattern usernamePattern = Pattern.compile("Successfully set your username to \"(.*)\"");
	
	@Override
	public void onPacket(Packet packet) {
		if (packet instanceof ClientboundChatPacket) {
			ClientboundChatPacket t_packet = (ClientboundChatPacket) packet;
			
			Component message = t_packet.getMessage();
			String jsonMessage = gson().serialize(message);
			String strMessage = ChatUtils.getFullText(message);
			String sanitizedName = bot.getUsername().replaceAll("§[0-9a-frlonmk]", "");
			Matcher matcher;
			if (strMessage.startsWith("You have been muted!")) {
				muted = true;
			}
			else if (strMessage.startsWith("Your voice has been silenced!") || strMessage.startsWith("Your voice has been silenced for ")) {
				muted = true;
			}
			else if ((matcher = mutePattern.matcher(strMessage)).matches()) {
				if (matcher.group(1).equals("now")) {
					muted = false;
				}
				else {
					muted = true;
				}
			}
			else if (jsonMessage.equals("{\"text\":\"\",\"extra\":[{\"text\":\"You have been unmuted.\",\"color\":\"gold\"}]}")) {
				muted = false;
			}
			else if (jsonMessage.equals("{\"text\":\"\",\"extra\":[{\"text\":\"You can now talk again.\",\"color\":\"gold\"}]}")) {
				muted = false;
			}
			else if (strMessage.equals("Vanish for " + sanitizedName + ": disabled")) {
				vanish = false;
			}
			else if (jsonMessage.equals("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gold\",\"text\":\"You are now completely invisible to normal users, and hidden from in-game commands.\"}],\"text\":\"\"}")) {
				vanish = true;
			}
			else if (jsonMessage.equals("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gold\",\"text\":\"God mode\"},{\"italic\":false,\"color\":\"red\",\"text\":\" enabled\"},{\"italic\":false,\"color\":\"gold\",\"text\":\".\"}],\"text\":\"\"}")) {
				godmode = true;
			}
			else if (jsonMessage.equals("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gold\",\"text\":\"God mode\"},{\"italic\":false,\"color\":\"red\",\"text\":\" disabled\"},{\"italic\":false,\"color\":\"gold\",\"text\":\".\"}],\"text\":\"\"}")) {
				godmode = false;
			}
			else if (jsonMessage.equals("{\"extra\":[{\"text\":\"Successfully disabled CommandSpy\"}],\"text\":\"\"}")) {
				commandspy = false;
			}
			else if (jsonMessage.equals("{\"extra\":[{\"text\":\"Successfully enabled CommandSpy\"}],\"text\":\"\"}")) {
				commandspy = true;
			}
			else if ((matcher = usernamePattern.matcher(strMessage)).matches()) {
				if (matcher.group(1).equals(sanitizedName)) {
					wrongUsername = false;
				}
				else {
					wrongUsername = true;
				}
			}
			else if (strMessage.startsWith("You already have the username ")) {
				wrongUsername = false;
			}
			else if (strMessage.equals("A player with that username is already logged in")) {
				wrongUsername = false;
			}
			else if (strMessage.startsWith("Your nickname is now ")) {
            	wrongNick = true;
            }
            else if (strMessage.equals("You no longer have a nickname.")) {
            	wrongNick = false;
            }
            else if (strMessage.startsWith("You now have the tag: ")) {
            	wrongPrefix = true;
            }
            else if (strMessage.equals("You no longer have a tag")) {
            	wrongPrefix = false;
            }
		}
		else if (packet instanceof ClientboundGameEventPacket) {
			ClientboundGameEventPacket t_packet = (ClientboundGameEventPacket) packet;
        	if (t_packet.getNotification() == GameEvent.CHANGE_GAMEMODE) {
        		gamemode = (GameMode) t_packet.getValue();
        	}
        }
		else if (packet instanceof ClientboundLoginPacket) {
			ClientboundLoginPacket t_packet = (ClientboundLoginPacket) packet;
			entityId = t_packet.getEntityId();
			gamemode = t_packet.getGameMode();
			dimension = ((StringTag)t_packet.getDimension().get("effects")).getValue();
		}
		else if (packet instanceof ClientboundEntityEventPacket) {
			ClientboundEntityEventPacket t_packet = (ClientboundEntityEventPacket) packet;
			if (t_packet.getEntityId() == entityId) {
				if (t_packet.getStatus() == EntityEvent.PLAYER_OP_PERMISSION_LEVEL_0) {
					opped = false;
				}
				else if (t_packet.getStatus() == EntityEvent.PLAYER_OP_PERMISSION_LEVEL_4) {
					opped = true;
				}
			}
		}
		else if (packet instanceof ClientboundRespawnPacket) {
			ClientboundRespawnPacket t_packet = (ClientboundRespawnPacket) packet;
			gamemode = t_packet.getGamemode();
			dimension = ((StringTag)t_packet.getDimension().get("effects")).getValue();
		}
	}
	
	@Override
	public void onDisconnected(DisconnectedEvent event) {
		opped = true;
		vanish = false;
		commandspy = false;
		gamemode = GameMode.CREATIVE;
		godmode = false;
		wrongUsername = false;
	}

	@Override
	public void onTick() {
		if (System.currentTimeMillis() >= nextRectifyTime) {
			rectify();
		}
	}
	
	public void rectify() {
		nextRectifyTime = System.currentTimeMillis();
		if (autoOp && !opped) {
			bot.sendCommand("/op @s[type=player]");
			nextRectifyTime += commandDelay;
		}
		if (autoUnmute && muted) {
			bot.sendCommand("/mute " + bot.getUuid() + " 0s");
			nextRectifyTime += commandDelay;
		}
		if (autoVanish && vanish != targetVanish) {
			if (targetVanish == true) {
				bot.sendCommand("/v on");
			}
			else {
				bot.sendCommand("/v off");
			}
			nextRectifyTime += commandDelay;
		}
		if (autoGamemode && gamemode != targetGamemode) {
			if (targetGamemode == GameMode.SURVIVAL) {
				bot.sendCommand("/gms");
			}
			else if (targetGamemode == GameMode.CREATIVE) {
				bot.sendCommand("/gmc");
			}
			else if (targetGamemode == GameMode.ADVENTURE) {
				bot.sendCommand("/gma");
			}
			else if (targetGamemode == GameMode.SPECTATOR) {
				bot.sendCommand("/gmsp");
			}
			nextRectifyTime += commandDelay;
		}
		if (autoGodmode && !godmode) {
			bot.sendCommand("/god on");
			nextRectifyTime += commandDelay;
		}
		if (autoCommandspy && !commandspy) {
			bot.sendCommand("/c on");
			nextRectifyTime += commandDelay;
		}
		if (autoUsername && wrongUsername) {
			bot.sendCommand("/username &r" + bot.getUsername().replaceAll("§", "&"));
			nextRectifyTime += commandDelay;
		}
		if (autoNick && wrongNick) {
			bot.sendCommand("/essentials:nick off");
			nextRectifyTime += commandDelay;
		}
		if (autoPrefix && wrongPrefix) {
			bot.sendCommand("/extras:tag off");
			nextRectifyTime += commandDelay;
		}
	}
}
