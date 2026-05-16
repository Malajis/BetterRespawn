package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("betterrespawn.admin")) {
            autoRespawn(player);
            return;
        }

        deductExperience(player);
        if (plugin.getConfigManager().isEnableFeature()) {
            Location safeLoc = getSafeDeathLocation(player);
            if (safeLoc != null) deathLocations.put(uuid, safeLoc);
        }
        autoRespawn(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        RespawnTask old = plugin.getRespawningPlayers().remove(uuid);
        if (old != null) old.cancel();

        boolean admin = player.hasPermission("betterrespawn.admin");
        boolean featureOn = plugin.getConfigManager().isEnableFeature();

        if (admin || !featureOn) {
            Utils.healPlayer(player);
            return;
        }

        Location deathLoc = deathLocations.remove(uuid);
        if (deathLoc == null) {
            Utils.healPlayer(player);
            return;
        }

        event.setRespawnLocation(deathLoc);
        Location finalLoc = deathLoc.clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                player.teleport(finalLoc);
                RespawnTask task = new RespawnTask(plugin, player);
                plugin.getRespawningPlayers().put(uuid, task);
                task.start();
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        RespawnTask task = plugin.getRespawningPlayers().remove(uuid);
        if (task != null) task.cancel();
        deathLocations.remove(uuid);
    }

    private void autoRespawn(Player player) {
        if (!plugin.getConfigManager().isAutoRespawn()) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && player.isDead()) {
                    try { player.spigot().respawn(); }
                    catch (Exception ignored) {}
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void deductExperience(Player player) {
        if (!player.isOnline()) return;
        double ratio = plugin.getConfigManager().getExperienceCost();
        int deducted = (int) Math.floor(player.getLevel() * ratio);
        int newLevel = Math.max(0, player.getLevel() - deducted);
        player.setLevel(newLevel);
        player.setExp(0);

        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                + ChatColor.GRAY + "因死亡扣除 " + ChatColor.RED + deducted
                + ChatColor.GRAY + " 级 (" + (int) (ratio * 100) + "%)");
    }

    private Location getSafeDeathLocation(Player player) {
        Location deathLoc = player.getLocation().clone();
        if (Utils.isLocationVoid(deathLoc)) {
            Location spawn = player.getWorld().getSpawnLocation();
            Location ground = Utils.findGroundLocation(spawn);
            return ground != null ? ground : spawn;
        }
        return deathLoc;
    }

    private boolean shouldCancel(Player player) {
        if (player == null) return false;
        if (player.hasPermission("betterrespawn.admin")) return false;
        if (!plugin.getConfigManager().isEnableFeature()) return false;
        return plugin.getRespawningPlayers().containsKey(player.getUniqueId());
    }

    @EventHandler public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && shouldCancel(p)) e.setCancelled(true);
    }
    @EventHandler public void onPlayerInteract(PlayerInteractEvent e) {
        if (shouldCancel(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler public void onBlockBreak(BlockBreakEvent e) {
        if (shouldCancel(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler public void onBlockPlace(BlockPlaceEvent e) {
        if (shouldCancel(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (shouldCancel(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler public void onEntityPickupItem(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && shouldCancel(p)) e.setCancelled(true);
    }
    @EventHandler public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (shouldCancel(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && shouldCancel(p)) e.setCancelled(true);
    }
}
