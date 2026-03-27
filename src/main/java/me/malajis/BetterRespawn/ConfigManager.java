package me.malajis.BetterRespawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean enableFeature;
    private int respawnTime;
    private boolean autoRespawn;
    private int experienceCost;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        enableFeature = config.getBoolean("enable-feature", false);
        respawnTime = config.getInt("respawn-time", 5);
        autoRespawn = config.getBoolean("auto-respawn", true);
        experienceCost = config.getInt("experience-cost", 25);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reloadConfig() {
        loadConfig();
    }

    public boolean isEnableFeature() { return enableFeature; }
    public int getRespawnTime() { return respawnTime; }
    public boolean isAutoRespawn() { return autoRespawn; }
    public int getExperienceCost() { return experienceCost; }
}