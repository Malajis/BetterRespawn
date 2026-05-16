package me.malajis.BetterRespawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private final Map<UUID, RespawnTask> respawningPlayers = new HashMap<>();
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Objects.requireNonNull(getCommand("betterrespawn"))
                .setExecutor(new CommandManager(this));
        getLogger().info("BetterRespawn 已启用");
    }

    @Override
    public void onDisable() {
        respawningPlayers.values().forEach(RespawnTask::cancel);
        respawningPlayers.clear();
        getLogger().info("BetterRespawn 已禁用");
    }

    public Map<UUID, RespawnTask> getRespawningPlayers() {
        return respawningPlayers;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
