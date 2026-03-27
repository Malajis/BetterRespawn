package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RespawnTask {

    private final Main plugin;
    private final Player player;
    private int secondsLeft;
    private BukkitRunnable task;
    private final GameMode originalGameMode;
    private final boolean originalFlight;

    public RespawnTask(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.secondsLeft = plugin.getConfigManager().getRespawnTime();
        this.originalGameMode = player.getGameMode();
        this.originalFlight = player.getAllowFlight();
    }

    public void start() {
        // 设置倒计时状态
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, secondsLeft * 20, 255, false, false));

        playSound("ENTITY_PLAYER_RESPAWN", "ITEM_TOTEM_USE", 1.0f, 1.0f);

        task = new BukkitRunnable() {
            @Override
            public void run() {
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

    /** 安全播放音效（兼容低版本） */
    private void playSound(String sound1, String sound2, float vol, float pitch) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(sound1), vol, pitch);
        } catch (Exception e) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound2), vol, pitch);
            } catch (Exception ignored) {}
        }
    }

    /** 更新倒计时显示 */
    private void update() {
        player.sendTitle("§c复活倒计时", "§e" + secondsLeft + " 秒后复活 §7| 飞行到安全位置", 0, 60, 0);
        playSound(secondsLeft <= 3 ? "BLOCK_NOTE_BLOCK_PLING" : "UI_BUTTON_CLICK",
                secondsLeft <= 3 ? "NOTE_PLING" : "CLICK", 1.0f, secondsLeft <= 3 ? 2.0f : 1.0f);

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    /** 倒计时结束，完成复活 */
    private void finish() {
        plugin.getRespawningPlayers().remove(player.getUniqueId());

        Location ground = findGroundLocation(player.getLocation());

        if (ground != null) {
            player.teleport(ground);
            playSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.0f, 1.0f);
            // 渐入渐出效果：淡入0.5秒，显示1.5秒，淡出0.5秒
            player.sendTitle("§a复活成功！", "§e你现在可以正常游戏了", 10, 30, 10);
        } else {
            // 虚空处理
            Location spawn = player.getWorld().getSpawnLocation();
            Location safe = findGroundLocation(spawn);
            player.teleport(safe != null ? safe : spawn);

            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                    + ChatColor.RED + "警告 " + ChatColor.GRAY + "脚下是虚空，已传送到重生点");

            playSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.0f, 1.0f);
        }

        // 恢复原始状态
        player.setGameMode(originalGameMode);
        player.setAllowFlight(originalFlight);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.setHealth(20);
    }

    /** 查找脚下最近的固体方块位置 */
    private Location findGroundLocation(Location loc) {
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Location check = loc.clone();
            check.setY(y);
            if (check.getBlock().getType().isSolid()) {
                return check.clone().add(0.5, 1, 0.5);
            }
        }
        return null;
    }

    /** 取消倒计时 */
    public void cancel() {
        if (task != null && !task.isCancelled()) task.cancel();
        player.setGameMode(originalGameMode);
        player.setAllowFlight(originalFlight);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        plugin.getRespawningPlayers().remove(player.getUniqueId());
    }
}