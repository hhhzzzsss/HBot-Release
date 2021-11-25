package com.github.hhhzzzsss.hbot.entity;

import java.util.UUID;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;

import lombok.*;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
@Getter
@Setter
public class PlayerData {
	private @NonNull GameProfile profile;
    private GameMode gameMode;
    private int ping;
    private Component displayName;
    
    public String getName() {
    	return profile.getName();
    }
    
    public UUID getUUID() {
    	return profile.getId();
    }
    
    public static PlayerData fromEntry(PlayerListEntry entry) {
    	return new PlayerData(entry.getProfile(), entry.getGameMode(), entry.getPing(), entry.getDisplayName());
    }
}
