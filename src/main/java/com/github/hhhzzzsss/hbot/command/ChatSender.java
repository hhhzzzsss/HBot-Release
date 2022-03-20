package com.github.hhhzzzsss.hbot.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ChatSender {
    private final UUID uuid;
    private final String name;
    private final String displayName;
}
