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

/**
 * 玩家事件监听器
 * 处理玩家死亡、重生、退出等相关事件
 */
public class PlayerListener implements Listener {

    private final Main plugin;
    
    /** 玩家死亡位置映射 */
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件主类实例
     */
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * 玩家死亡事件处理
     * @param event 玩家死亡事件
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

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

    /**
     * 自动重生（如果配置开启）
     * @param player 目标玩家
     */
    private void autoRespawnIfEnabled(Player player) {
        if (player == null) return;
        if (plugin.getConfigManager().isAutoRespawn()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && player.isDead()) {
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

    /**
     * 扣除等级并发送消息
     * @param player 目标玩家
     */
    private void deductExperience(Player player) {
        if (player == null || !player.isOnline()) return;
        
        double ratio = plugin.getConfigManager().getExperienceCost();
        int originalLevel = player.getLevel();
        int newLevel = Math.max(0, (int) Math.floor(originalLevel * (1 - ratio)));
        int levelsDeducted = originalLevel - newLevel;

        player.setLevel(newLevel);
        player.setExp(0);

        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                + ChatColor.GRAY + "因死亡扣除 " + ChatColor.RED + levelsDeducted
                + ChatColor.GRAY + " 级 (" + (int)(ratio * 100) + "%)");
    }

    /**
     * 获取安全的死亡位置（虚空时传送到重生点）
     * @param player 目标玩家
     * @return 安全的死亡位置
     */
    private Location getSafeDeathLocation(Player player) {
        if (player == null || player.getWorld() == null) return null;
        
        Location deathLoc = player.getLocation().clone();
        if (Utils.isLocationVoid(deathLoc)) {
            Location spawn = player.getWorld().getSpawnLocation();
            Location ground = Utils.findGroundLocation(spawn);
            return ground != null ? ground : spawn;
        }
        return deathLoc;
    }

    /**
     * 玩家重生事件处理
     * @param event 玩家重生事件
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // 先清理可能残留的任务
        RespawnTask oldTask = plugin.getRespawningPlayers().remove(player.getUniqueId());
        if (oldTask != null) {
            oldTask.cancel();
        }

        // 管理员直接复活
        if (player.hasPermission("betterrespawn.admin")) {
            Utils.healPlayer(player);
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
                        if (player.isOnline()) {
                            player.teleport(deathLoc);
                            RespawnTask task = new RespawnTask(plugin, player);
                            plugin.getRespawningPlayers().put(player.getUniqueId(), task);
                            task.start();
                        }
                    }
                }.runTaskLater(plugin, 1L);
            } else {
                // 如果没有死亡位置，直接复活
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            Utils.healPlayer(player);
                        }
                    }
                }.runTaskLater(plugin, 2L);
            }
        } else {
            // 功能关闭：直接复活
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        Utils.healPlayer(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    /**
     * 玩家退出事件处理
     * @param event 玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        RespawnTask task = plugin.getRespawningPlayers().remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        deathLocations.remove(event.getPlayer().getUniqueId());
    }

    // ==================== 限制类事件 ====================
    
    /**
     * 判断是否应该取消玩家的操作
     * 条件：倒计时中 + 非管理员 + 功能开启
     * @param player 目标玩家
     * @return 是否应该取消操作
     */
    private boolean shouldCancel(Player player) {
        if (player == null) return false;
        if (player.hasPermission("betterrespawn.admin")) return false;
        // 如果功能没有开启，绝对不要限制玩家
        if (!plugin.getConfigManager().isEnableFeature()) return false;
        // 只有在功能开启且玩家确实在倒计时Map中时才限制
        return plugin.getRespawningPlayers() != null && 
               plugin.getRespawningPlayers().containsKey(player.getUniqueId());
    }

    /** 实体伤害事件 */
    @EventHandler public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && shouldCancel((Player) e.getEntity())) e.setCancelled(true);
    }
    /** 玩家交互事件 */
    @EventHandler public void onPlayerInteract(PlayerInteractEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 方块破坏事件 */
    @EventHandler public void onBlockBreak(BlockBreakEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 方块放置事件 */
    @EventHandler public void onBlockPlace(BlockPlaceEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 玩家丢弃物品事件 */
    @EventHandler public void onPlayerDropItem(PlayerDropItemEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 玩家拾取物品事件 */
    @EventHandler public void onPlayerPickupItem(PlayerPickupItemEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 玩家与实体交互事件 */
    @EventHandler public void onPlayerInteractEntity(PlayerInteractEntityEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 背包点击事件 */
    @EventHandler public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player && shouldCancel((Player) e.getWhoClicked())) e.setCancelled(true);
    }
    /** 玩家命令执行事件 */
    @EventHandler public void onPlayerCommand(PlayerCommandPreprocessEvent e) { if (shouldCancel(e.getPlayer())) e.setCancelled(true); }
    /** 玩家移动事件（允许移动） */
    @EventHandler public void onPlayerMove(PlayerMoveEvent e) { /* 允许移动 */ }
}
