package me.malajis.BetterRespawn;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class Utils {

    private static final int WORLD_MIN_Y = -64;

    private Utils() {}

    public static Location findGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (int y = loc.getBlockY(); y >= WORLD_MIN_Y; y--) {
            if (isSolidAt(loc, y)) return loc.clone().add(0.5, y + 1.5 - loc.getY(), 0.5);
        }
        return null;
    }

    public static boolean isLocationVoid(Location loc) {
        if (loc == null || loc.getWorld() == null) return true;
        for (int y = loc.getBlockY(); y >= WORLD_MIN_Y; y--) {
            if (isSolidAt(loc, y)) return false;
        }
        return true;
    }

    private static boolean isSolidAt(Location loc, int y) {
        Location check = loc.clone();
        check.setY(y);
        return check.getBlock().getType().isSolid();
    }

    public static void playSound(Player player, String sound1, String sound2, float vol, float pitch) {
        if (player == null || !player.isOnline()) return;
        try {
            player.playSound(player.getLocation(), Sound.valueOf(sound1), vol, pitch);
        } catch (IllegalArgumentException e1) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound2), vol, pitch);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static void healPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        player.setHealth(player.getMaxHealth());
        player.getActivePotionEffects()
                .forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}
