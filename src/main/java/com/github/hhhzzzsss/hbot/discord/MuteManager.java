package com.github.hhhzzzsss.hbot.discord;

import com.github.hhhzzzsss.hbot.command.CommandException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MuteManager {

    @Getter private static Set<String> mutelist = Collections.synchronizedSet(new TreeSet<>());

    private static Gson gson = new Gson();
    @Getter private static final File mutelistFile = new File("mutelist.json");

    static {
        readMuteList();
    }

    public static void readMuteList() {
        if (mutelistFile.exists()) {
            InputStreamReader isReader;
            try {
                isReader = new InputStreamReader(new FileInputStream(mutelistFile), "UTF-8");

                JsonReader jsonReader = new JsonReader(isReader);
                Type treeSetType = new TypeToken<TreeSet<String>>(){}.getType();
                synchronized (mutelist) {
                    mutelist = gson.fromJson(jsonReader, treeSetType);
                }
                jsonReader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeMuteList() {
        String json;
        synchronized(mutelist) {
            json = gson.toJson(mutelist);
        }
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(mutelistFile);

            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(json);
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mute(Member member) throws CommandException {
        mutelist.add(member.getUser().getAsTag());
        Guild guild = member.getGuild();
        List<Role> roles = guild.getRolesByName("muted", true);
        if (roles.size() == 0) throw new CommandException("Muted role does not seem to exist");
        guild.addRoleToMember(member, roles.get(0)).queue();
    }

    public static void unmute(Member member) {
        mutelist.remove(member.getUser().getAsTag());
        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase("muted")) {
                member.getGuild().removeRoleFromMember(member, role).queue();
            }
        }
    }

    public static void checkMember(Member member) {
        if (mutelist.contains(member.getUser().getAsTag())) {
            Guild guild = member.getGuild();
            List<Role> roles = guild.getRolesByName("muted", true);
            if (roles.size() > 0) {
                guild.addRoleToMember(member, roles.get(0)).queue();
            }
        } else {
            for (Role role : member.getRoles()) {
                if (role.getName().equalsIgnoreCase("muted")) {
                    member.getGuild().removeRoleFromMember(member, role).queue();
                }
            }
        }
    }
}
