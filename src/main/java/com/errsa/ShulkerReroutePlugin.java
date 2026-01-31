package com.errsa;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ShulkerReroutePlugin extends JavaPlugin {
    private WorldGuardHook wgHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Register /shulkerbay
        var cmd = getCommand("shulkerbay");
        if (cmd != null) {
            cmd.setExecutor(new ShulkerBayCommand(this));
        } else {
            getLogger().warning("Command 'shulkerbay' not found in plugin.yml");
        }


        wgHook = new WorldGuardHook(this);
        Bukkit.getPluginManager().registerEvents(new ShulkerTeleportListener(this, wgHook), this);

        getLogger().info("ShulkerReroute enabled. WG hook: " + (wgHook.isAvailable() ? "YES" : "NO"));
    }

    public Location getSafeDestination() {
        FileConfiguration c = getConfig();
        String wName = c.getString("safe-destination.world", "world");
        World w = Bukkit.getWorld(wName);
        if (w == null) w = Bukkit.getWorlds().get(0);

        double x = c.getDouble("safe-destination.x");
        double y = c.getDouble("safe-destination.y");
        double z = c.getDouble("safe-destination.z");
        float yaw = (float) c.getDouble("safe-destination.yaw", 0.0);
        float pitch = (float) c.getDouble("safe-destination.pitch", 0.0);

        return new Location(w, x, y, z, yaw, pitch);
    }

    public int getSearchRadius() {
        return Math.max(0, getConfig().getInt("search-radius", 8));
    }

    public boolean onlyWhenBlockedByWG() {
        return getConfig().getBoolean("only-when-blocked-by-worldguard", true);
    }
    public boolean isDebug() {
        return getConfig().getBoolean("debug", false);
    }

}
