package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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

        // 管理员跳过所有处理
        if (player.hasPermission("betterrespawn.admin")) {
            autoRespawnIfEnabled(player);
            return;
        }

        // 扣除经验
        deductExperience(player);

        // 保存死亡位置（仅当功能开启时）
        if (plugin.getConfigManager().isEnableFeature()) {
            deathLocations.put(player.getUniqueId(), getSafeDeathLocation(player));
        }

        // 自动重生
        autoRespawnIfEnabled(player);
    }

    /** 自动重生（如果配置开启） */
    private void autoRespawnIfEnabled(Player player) {
        if (plugin.getConfigManager().isAutoRespawn()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player != null && player.isDead()) {
                        try {
                            player.spigot().respawn();
                        } catch (Exception e) {
                            plugin.getLogger().warning("自动重生失败: " + e.getMessage());
                        }
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /** 扣除经验并发送消息 */
    private void deductExperience(Player player) {
        int percent = plugin.getConfigManager().getExperienceCost();
        int current = player.getTotalExperience();
        int deduct = current * percent / 100;
        int remaining = Math.max(0, current - deduct);

        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(remaining);

        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                + ChatColor.GRAY + "因死亡扣除 " + ChatColor.RED + deduct
                + ChatColor.GRAY + " 经验 (" + percent + "%)");
    }

    /** 获取安全的死亡位置（虚空时传送到重生点） */
    private Location getSafeDeathLocation(Player player) {
        Location deathLoc = player.getLocation().clone();
        if (isLocationVoid(deathLoc)) {
            Location spawn = player.getWorld().getSpawnLocation();
            Location ground = findGroundLocation(spawn);
            return ground != null ? ground : spawn;
        }
        return deathLoc;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 管理员直接复活
        if (player.hasPermission("betterrespawn.admin")) {
            healPlayer(player);
            return;
        }

        // 功能开启：原地复活 + 倒计时
        if (plugin.getConfigManager().isEnableFeature()) {
            Location deathLoc = deathLocations.remove(player.getUniqueId());
            if (deathLoc != null) {
                event.setRespawnLocation(deathLoc);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player != null && player.isOnline()) {
                            player.teleport(deathLoc);
                            RespawnTask task = new RespawnTask(plugin, player);
                            plugin.getRespawningPlayers().put(player.getUniqueId(), task);
                            task.start();
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else {
            // 功能关闭：直接复活
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player != null && player.isOnline()) {
                        healPlayer(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    /** 恢复玩家生命并清除负面效果 */
    private void healPlayer(Player player) {
        player.setHealth(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Object task = plugin.getRespawningPlayers().remove(event.getPlayer().getUniqueId());
        if (task != null) {
            try {
                task.getClass().getMethod("cancel").invoke(task);
            } catch (Exception e) {
                // 忽略取消时的异常
            }
        }
        deathLocations.remove(event.getPlayer().getUniqueId());
    }

    /** 检测位置是否在虚空（下方无固体方块） */
    private boolean isLocationVoid(Location loc) {
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Location check = loc.clone();
            check.setY(y);
            if (check.getBlock().getType().isSolid()) return false;
        }
        return true;
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

    // ==================== 限制类事件 ====================
    /** 判断是否应该取消玩家的操作（倒计时中 + 非管理员 + 功能开启） */
    private boolean shouldCancel(Player player) {
        if (player == null) return false;
        if (player.hasPermission("betterrespawn.admin")) return false;
        if (!plugin.getConfigManager().isEnableFeature()) return false;
        return plugin.getRespawningPlayers().containsKey(player.getUniqueId());
    }

    @EventHandler public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && shouldCancel((Player) e.getEntity())) e.setCancelled(true);
    }
    @EventHandler public void onPlayerInteract(PlayerInteractEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onBlockBreak(BlockBreakEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onBlockPlace(BlockPlaceEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPlayerDropItem(PlayerDropItemEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPlayerPickupItem(PlayerPickupItemEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPlayerInteractEntity(PlayerInteractEntityEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player && shouldCancel((Player) e.getWhoClicked())) e.setCancelled(true);
    }
    @EventHandler public void onPlayerCommand(PlayerCommandPreprocessEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPlayerMove(PlayerMoveEvent e) { /* 允许移动 */ }
}