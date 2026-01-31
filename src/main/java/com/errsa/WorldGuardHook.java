package com.errsa;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook {
    private final ShulkerReroutePlugin plugin;
    private final boolean available;

    // Optional: also check deny-spawn if you use it
    private final com.sk89q.worldguard.protection.flags.SetFlag<String> denySpawnFlag;

    public WorldGuardHook(ShulkerReroutePlugin plugin) {
        this.plugin = plugin;

        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.available = (wg != null) && wg.isEnabled();

        // Safely try to resolve deny-spawn flag (exists in WG)
        com.sk89q.worldguard.protection.flags.SetFlag<String> tmp = null;
        if (available) {
            FlagRegistry reg = WorldGuard.getInstance().getFlagRegistry();
            try {
                tmp = (com.sk89q.worldguard.protection.flags.SetFlag<String>) reg.get("deny-spawn");
            } catch (Exception ignored) {}
        }
        this.denySpawnFlag = tmp;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isMobSpawningDenied(Location loc) {
        if (!available || loc.getWorld() == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (regions == null) return false;

        var weLoc = BukkitAdapter.adapt(loc);
        ApplicableRegionSet set = regions.getApplicableRegions(weLoc.toVector().toBlockPoint());


        // 1) mob-spawning: DENY => blocked
        StateFlag.State mobSpawn = set.queryState(null, Flags.MOB_SPAWNING);
        if (mobSpawn == StateFlag.State.DENY) return true;

        // 2) deny-spawn contains "shulker" or "minecraft:shulker" (depends how you set it)
        if (denySpawnFlag != null) {
            var deny = set.queryValue(null, denySpawnFlag);
            if (deny != null) {
                for (String s : deny) {
                    String t = s.toLowerCase();
                    if (t.contains("shulker")) return true;
                }
            }
        }

        return false;
    }
}
