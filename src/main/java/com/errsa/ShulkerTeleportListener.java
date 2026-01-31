package com.errsa;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;

public class ShulkerTeleportListener implements Listener {

    private final ShulkerReroutePlugin plugin;
    private final WorldGuardHook wg;

    public ShulkerTeleportListener(ShulkerReroutePlugin plugin, WorldGuardHook wg) {
        this.plugin = plugin;
        this.wg = wg;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        var entity = event.getEntity();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;

        boolean isShulker = entity.getType() == EntityType.SHULKER;

        boolean hasShulkerPassenger = entity.getPassengers().stream()
                .anyMatch(p -> p.getType() == EntityType.SHULKER);

        // Only care about shulkers OR vehicles carrying shulkers
        if (!isShulker && !hasShulkerPassenger) return;

        boolean blockedByWG = wg.isAvailable() && wg.isMobSpawningDenied(to);
        if (plugin.onlyWhenBlockedByWG() && !blockedByWG) return;

        Location safeBase = plugin.getSafeDestination().clone();
        Location safe = findNearbySafeSpot(safeBase, plugin.getSearchRadius());

        if (plugin.isDebug()) {
            plugin.getLogger().info(
                    "Rerouting teleport: entity=" + entity.getType()
                            + " from=" + event.getFrom()
                            + " to=" + event.getTo()
                            + " -> " + safe
            );
        }


        event.setTo(safe);
    }

    private Location findNearbySafeSpot(Location base, int r) {
        World w = base.getWorld();
        if (w == null) return base;

        if (isStandable(base)) return center(base);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                Location test = base.clone().add(dx, 0, dz);
                for (int dy = 0; dy <= 10; dy++) {
                    Location up = test.clone().add(0, dy, 0);
                    if (isStandable(up)) return center(up);
                    Location down = test.clone().add(0, -dy, 0);
                    if (isStandable(down)) return center(down);
                }
            }
        }
        return base;
    }

    private boolean isStandable(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block below = loc.clone().add(0, -1, 0).getBlock();
        return isAirish(feet) && isAirish(head) && below.getType().isSolid();
    }

    private boolean isAirish(Block b) {
        Material m = b.getType();
        return m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR;
    }

    private Location center(Location loc) {
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getY(),
                loc.getBlockZ() + 0.5,
                loc.getYaw(),
                loc.getPitch());
    }
}
