package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RespawnTask {

    private final Main plugin;
    private final Player player;
    private int secondsLeft;
    private final GameMode originalGameMode;
    private final boolean originalFlight;
    private BukkitRunnable task;
    private boolean finished;

    public RespawnTask(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.secondsLeft = Math.max(1, plugin.getConfigManager().getRespawnTime());
        this.originalGameMode = player.getGameMode();
        this.originalFlight = player.getAllowFlight();
    }

    public void start() {
        if (!player.isOnline()) return;

        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0);
        player.setHealth(player.getMaxHealth());
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, secondsLeft * 20, 255, true, false, false));

        Utils.playSound(player, "ENTITY_PLAYER_RESPAWN", "ITEM_TOTEM_USE", 1.0f, 1.0f);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                if (secondsLeft <= 0) { finish(); this.cancel(); return; }
                update();
                secondsLeft--;
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void update() {
        if (!player.isOnline()) return;

        player.sendTitle("§c复活倒计时", "§e" + secondsLeft + " 秒后复活 §7| 飞行到安全位置", 0, 60, 0);
        Utils.playSound(player,
                secondsLeft <= 3 ? "BLOCK_NOTE_BLOCK_PLING" : "UI_BUTTON_CLICK",
                secondsLeft <= 3 ? "BLOCK_NOTE_BLOCK_BELL" : "BLOCK_STONE_BUTTON_CLICK_ON",
                1.0f, secondsLeft <= 3 ? 2.0f : 1.0f);

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    private void finish() {
        if (!player.isOnline() || finished) return;
        finished = true;

        plugin.getRespawningPlayers().remove(player.getUniqueId());

        Location ground = Utils.findGroundLocation(player.getLocation());
        if (ground != null) {
            player.teleport(ground);
            player.sendTitle("§a复活成功！", "§e你现在可以正常游戏了", 10, 30, 10);
        } else {
            Location spawn = player.getWorld() != null ? player.getWorld().getSpawnLocation() : null;
            Location safe = spawn != null ? Utils.findGroundLocation(spawn) : null;
            player.teleport(safe != null ? safe : spawn);
            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "重生系统" + ChatColor.DARK_GRAY + "] "
                    + ChatColor.RED + "警告 " + ChatColor.GRAY + "脚下是虚空，已传送到重生点");
        }
        Utils.playSound(player, "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP", 1.0f, 1.0f);

        restoreState();
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) task.cancel();
        if (player.isOnline() && !finished) restoreState();
        plugin.getRespawningPlayers().remove(player.getUniqueId());
    }

    private void restoreState() {
        if (!player.isOnline()) return;
        player.setGameMode(originalGameMode);
        player.setAllowFlight(originalFlight);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.setHealth(player.getMaxHealth());
    }
}
