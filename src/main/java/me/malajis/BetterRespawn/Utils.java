package me.malajis.BetterRespawn;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 工具类
 * 提供通用的工具方法
 */
public class Utils {

    /**
     * 查找脚下最近的固体方块位置
     * @param loc 起始位置
     * @return 地面位置，找不到返回 null
     */
    public static Location findGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Location check = loc.clone();
            check.setY(y);
            if (check.getBlock().getType().isSolid()) {
                return check.clone().add(0.5, 1, 0.5);
            }
        }
        return null;
    }

    /**
     * 检测位置是否在虚空（下方无固体方块）
     * @param loc 要检测的位置
     * @return 是否在虚空
     */
    public static boolean isLocationVoid(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return true;
        }
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Location check = loc.clone();
            check.setY(y);
            if (check.getBlock().getType().isSolid()) return false;
        }
        return true;
    }

    /**
     * 安全播放音效（兼容低版本）
     * @param player 目标玩家
     * @param sound1 首选音效名称
     * @param sound2 备选音效名称
     * @param vol 音量
     * @param pitch 音调
     */
    public static void playSound(Player player, String sound1, String sound2, float vol, float pitch) {
        if (player == null || !player.isOnline()) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.valueOf(sound1), vol, pitch);
        } catch (Exception e) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound2), vol, pitch);
            } catch (Exception ignored) {}
        }
    }

    /**
     * 恢复玩家生命并清除负面效果
     * @param player 目标玩家
     */
    public static void healPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        player.setHealth(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}
