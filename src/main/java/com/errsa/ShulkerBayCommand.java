package com.errsa;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShulkerBayCommand implements CommandExecutor {

    private final ShulkerReroutePlugin plugin;

    public ShulkerBayCommand(ShulkerReroutePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("shulkerreroute.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
            Location l = player.getLocation();

            plugin.getConfig().set("safe-destination.world", l.getWorld().getName());
            plugin.getConfig().set("safe-destination.x", l.getX());
            plugin.getConfig().set("safe-destination.y", l.getY());
            plugin.getConfig().set("safe-destination.z", l.getZ());
            plugin.getConfig().set("safe-destination.yaw", l.getYaw());
            plugin.getConfig().set("safe-destination.pitch", l.getPitch());

            plugin.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Shulker bay destination set to:");
            sender.sendMessage(ChatColor.GRAY + l.getWorld().getName()
                    + " " + round(l.getX()) + " " + round(l.getY()) + " " + round(l.getZ()));
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /shulkerbay set");
        return true;
    }

    private String round(double v) {
        return String.format("%.2f", v);
    }
}
