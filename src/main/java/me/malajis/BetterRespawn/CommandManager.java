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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) { sendHelp(sender); return true; }
        if (args[0].equalsIgnoreCase("reload")) return handleReload(sender);
        sendHelp(sender);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("betterrespawn.reload")) {
            sender.sendMessage(ChatColor.RED + "[BetterRespawn] 你没有权限执行此命令！");
            return true;
        }
        try {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "[BetterRespawn] 配置已重载成功");
            plugin.getLogger().info("配置文件已被 " + sender.getName() + " 重载");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "[BetterRespawn] 配置重载失败！");
            plugin.getLogger().severe("配置文件重载失败: " + e.getMessage());
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ConfigManager cfg = plugin.getConfigManager();
        String sep = ChatColor.GOLD + "================================";
        sender.sendMessage(sep);
        sender.sendMessage(ChatColor.GOLD + " [ " + ChatColor.WHITE + "BetterRespawn" + ChatColor.GOLD + " ]");
        sender.sendMessage(sep);
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + " 命令:");
        sender.sendMessage(ChatColor.GRAY + "   " + ChatColor.WHITE + "/br reload" + ChatColor.GRAY + " - 重载配置文件");
        sender.sendMessage(ChatColor.GRAY + "   " + ChatColor.WHITE + "/br help" + ChatColor.GRAY + " - 显示帮助信息");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + " 配置状态:");
        sender.sendMessage(ChatColor.GRAY + "   功能开关: " + status(cfg.isEnableFeature()));
        sender.sendMessage(ChatColor.GRAY + "   自动重生: " + status(cfg.isAutoRespawn()));
        sender.sendMessage(ChatColor.GRAY + "   倒计时: " + ChatColor.WHITE + cfg.getRespawnTime() + " 秒");
        sender.sendMessage(ChatColor.GRAY + "   经验扣除: " + ChatColor.WHITE + (int) (cfg.getExperienceCost() * 100) + "%");
        sender.sendMessage("");
        sender.sendMessage(sep);
    }

    private String status(boolean on) {
        return on ? ChatColor.GREEN + "■ 启用" : ChatColor.RED + "■ 禁用";
    }
}
