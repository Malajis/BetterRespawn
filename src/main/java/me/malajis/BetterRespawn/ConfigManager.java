package me.malajis.BetterRespawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean enableFeature;
    private int respawnTime;
    private boolean autoRespawn;
    private double experienceCost;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        config.addDefault("enable-feature", true);
        config.addDefault("respawn-time", 5);
        config.addDefault("auto-respawn", true);
        config.addDefault("experience-cost", 0.25);
        config.options().copyDefaults(true);

        enableFeature = config.getBoolean("enable-feature", true);
        respawnTime = Math.max(1, config.getInt("respawn-time", 5));
        autoRespawn = config.getBoolean("auto-respawn", true);
        experienceCost = Math.max(0, Math.min(1, config.getDouble("experience-cost", 0.25)));
    }

    public boolean isEnableFeature() { return enableFeature; }
    public int getRespawnTime() { return respawnTime; }
    public boolean isAutoRespawn() { return autoRespawn; }
    public double getExperienceCost() { return experienceCost; }
}
