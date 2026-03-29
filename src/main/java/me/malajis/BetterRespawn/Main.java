package me.malajis.BetterRespawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BetterRespawn 插件主类
 * 功能：玩家死亡后原地复活并带有倒计时，倒计时期间可以飞行
 */
public class Main extends JavaPlugin {

    /** 当前正在复活倒计时的玩家映射 */
    private Map<UUID, RespawnTask> respawningPlayers;
    
    /** 配置管理器 */
    private ConfigManager configManager;

    /**
     * 插件启用时调用
     * 初始化数据结构、加载配置、注册事件和命令
     */
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

    /**
     * 插件禁用时调用
     * 安全地取消所有倒计时任务，清理资源
     */
    @Override
    public void onDisable() {
        if (respawningPlayers != null) {
            for (RespawnTask task : respawningPlayers.values()) {
                if (task != null) {
                    task.cancel();
                }
            }
            respawningPlayers.clear();
        }
        getLogger().info("BetterRespawn 插件已禁用");
    }

    /**
     * 获取正在复活倒计时的玩家映射
     * @return 玩家UUID到复活任务的映射
     */
    public Map<UUID, RespawnTask> getRespawningPlayers() {
        return respawningPlayers;
    }

    /**
     * 获取配置管理器
     * @return 配置管理器实例
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}