package me.malajis.BetterRespawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    private Map<UUID, Object> respawningPlayers;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        respawningPlayers = new HashMap<>();
        configManager = new ConfigManager(this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        if (getCommand("betterrespawn") != null) {
            getCommand("betterrespawn").setExecutor(new CommandManager(this));
            getLogger().info("命令注册成功");
        } else {
            getLogger().warning("命令注册失败");
        }

        getLogger().info("BetterRespawn 插件已启用");
    }

    @Override
    public void onDisable() {
        // 安全地取消所有倒计时任务
        if (respawningPlayers != null) {
            for (Object task : respawningPlayers.values()) {
                if (task != null) {
                    try {
                        // 使用反射调用 cancel 方法
                        task.getClass().getMethod("cancel").invoke(task);
                    } catch (Exception e) {
                        // 忽略取消时的异常
                    }
                }
            }
            respawningPlayers.clear();
        }
        getLogger().info("BetterRespawn 插件已禁用");
    }

    public Map<UUID, Object> getRespawningPlayers() {
        return respawningPlayers;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}