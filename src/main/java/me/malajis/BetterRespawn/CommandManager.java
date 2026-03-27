package me.malajis.BetterRespawn;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("betterrespawn.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        try {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "✓ BetterRespawn 配置已重载！");
            plugin.getLogger().info("配置文件已被 " + sender.getName() + " 重载");
            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✗ 重载配置失败！");
            plugin.getLogger().severe("配置文件重载失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void sendHelp(CommandSender sender) {
        ConfigManager cfg = plugin.getConfigManager();
        sender.sendMessage(ChatColor.GOLD + "===== BetterRespawn 帮助 =====");
        sender.sendMessage(ChatColor.YELLOW + "/betterrespawn reload " + ChatColor.WHITE + "- 重载配置文件");
        sender.sendMessage(ChatColor.YELLOW + "/betterrespawn help " + ChatColor.WHITE + "- 显示帮助信息");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "当前配置状态:");
        sender.sendMessage(ChatColor.GRAY + "  功能开关: " + getStatusColor(cfg.isEnableFeature()) + cfg.isEnableFeature());
        sender.sendMessage(ChatColor.GRAY + "  自动重生: " + getStatusColor(cfg.isAutoRespawn()) + cfg.isAutoRespawn());
        sender.sendMessage(ChatColor.GRAY + "  倒计时时间: " + ChatColor.WHITE + cfg.getRespawnTime() + "秒");
        sender.sendMessage(ChatColor.GRAY + "  经验扣除: " + ChatColor.WHITE + cfg.getExperienceCost() + "%");
    }

    private String getStatusColor(boolean status) {
        return status ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
    }
}