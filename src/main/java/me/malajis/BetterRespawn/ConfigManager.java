package me.malajis.BetterRespawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 配置管理器
 * 负责加载、保存和管理插件配置
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean enableFeature;
    private int respawnTime;
    private boolean autoRespawn;
    private double experienceCost;

    /**
     * 构造函数
     * @param plugin 插件主类实例
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        config.addDefault("enable-feature", false);
        config.addDefault("respawn-time", 5);
        config.addDefault("auto-respawn", true);
        config.addDefault("experience-cost", 0.25);

        enableFeature = config.getBoolean("enable-feature");
        respawnTime = config.getInt("respawn-time");
        autoRespawn = config.getBoolean("auto-respawn");
        experienceCost = config.getDouble("experience-cost");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * 重载配置文件
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * 获取功能是否开启
     * @return 功能是否开启
     */
    public boolean isEnableFeature() { return enableFeature; }
    
    /**
     * 获取倒计时时间
     * @return 倒计时时间（秒）
     */
    public int getRespawnTime() { return respawnTime; }
    
    /**
     * 获取是否自动重生
     * @return 是否自动重生
     */
    public boolean isAutoRespawn() { return autoRespawn; }
    
    /**
     * 获取等级扣除比例
     * @return 等级扣除比例（0-1）
     */
    public double getExperienceCost() { return experienceCost; }
}
