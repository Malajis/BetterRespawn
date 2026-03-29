package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 复活倒计时任务
 * 处理玩家复活后的倒计时、飞行权限、状态恢复等
 */
public class RespawnTask {

    private final Main plugin;
    private final Player player;
    private int secondsLeft;
    private BukkitRunnable task;
    private final GameMode originalGameMode;
    private final boolean originalFlight;

    /**
     * 构造函数
     * @param plugin 插件主类实例
     * @param player 目标玩家
     */
    public RespawnTask(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.secondsLeft = plugin.getConfigManager().getRespawnTime();
        this.originalGameMode = player.getGameMode();
        this.originalFlight = player.getAllowFlight();
    }

    /**
     * 开始倒计时任务
     */
    public void start() {
        if (player == null || !player.isOnline()) return;
        
        // 设置倒计时状态
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, secondsLeft * 20, 255, false, false));

        Utils.playSound(player, "ENTITY_PLAYER_RESPAWN", "ITEM_TOTEM_USE", 1.0f, 1.0f);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (secondsLeft <= 0) {
                    finish();
                    cancel();
                } else {
                    update();
                    secondsLeft--;
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * 更新倒计时显示
     */
    private void update() {
        if (!player.isOnline()) return;
        
        player.sendTitle("§c复活倒计时", "§e" + secondsLeft + " 秒后复活 §7| 飞行到安全位置", 0, 60, 0);
        Utils.playSound(player, secondsLeft <= 3 ? "BLOCK_NOTE_BLOCK_PLING" : "UI_BUTTON_CLICK",
                secondsLeft <= 3 ? "NOTE_PLING" : "CLICK", 1.0f, secondsLeft <= 3 ? 2.0f : 1.0f);

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    /**
     * 倒计时结束，完成复活
     */
    private void finish() {
        if (!player.isOnline()) return;
        
        plugin.getRespawningPlayers().remove(player.getUniqueId());

        Location ground = Utils.findGroundLocation(player.getLocation());

        if (ground != null) {
            player.teleport(ground);
            Utils.playSound(player, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.0f, 1.0f);
            player.sendTitle("§a复活成功！", "§e你现在可以正常游戏了", 10, 30, 10);
        } else {
            // 虚空处理
            if (player.getWorld() != null) {
                Location spawn = player.getWorld().getSpawnLocation();
                Location safe = Utils.findGroundLocation(spawn);
                player.teleport(safe != null ? safe : spawn);
            }

            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                    + ChatColor.RED + "警告 " + ChatColor.GRAY + "脚下是虚空，已传送到重生点");

            Utils.playSound(player, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.0f, 1.0f);
        }

        // 恢复原始状态
        player.setGameMode(originalGameMode);
        player.setAllowFlight(originalFlight);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.setHealth(20);
    }

    /**
     * 取消倒计时任务
     */
    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        if (player != null && player.isOnline()) {
            player.setGameMode(originalGameMode);
            player.setAllowFlight(originalFlight);
            player.setFlying(false);
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        }
        plugin.getRespawningPlayers().remove(player.getUniqueId());
    }
}
