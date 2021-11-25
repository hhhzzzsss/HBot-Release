package com.github.hhhzzzsss.hbot;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

@Data
public class Config {
    static final File file = new File("config.yml");
    @Getter static Config config;

    @Data
    public static class BotInfo {
        String host;
        int port;
        String serverNick;
        String discordToken;
        String categoryName;
    }

    ArrayList<BotInfo> bots;
    String trustedKey = "";
    String adminKey = "";
    String ownerKey = "";

    static {
        if (!file.exists()) {
            // creates config file from default-config.yml
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("default-config.yml");
            try {
                Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            IOUtils.closeQuietly(is);
        }
        Constructor constructor = new Constructor(Config.class);
        TypeDescription typeDescription = new TypeDescription(Config.class);
        typeDescription.addPropertyParameters("bots", BotInfo.class);
        constructor.addTypeDescription(typeDescription);
        Yaml yaml = new Yaml(constructor);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            config = yaml.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
