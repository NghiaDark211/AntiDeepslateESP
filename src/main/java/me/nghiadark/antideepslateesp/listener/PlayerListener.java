package me.nghiadark.antideepslateesp.listener;

import me.nghiadark.antideepslateesp.AntiDeepslateESP;
import me.nghiadark.antideepslateesp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class PlayerListener implements Listener {

    private final AntiDeepslateESP plugin;
    private final Map<UUID, Boolean> surfaceCache = new HashMap<>();

    public PlayerListener(AntiDeepslateESP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ConfigManager config = plugin.getConfigManager();
        int thresholdY = config.getThresholdY();

        Location to = event.getTo();
        if (to == null) return;

        boolean isSurface = to.getY() > thresholdY;
        UUID uuid = player.getUniqueId();

        Boolean wasSurface = surfaceCache.get(uuid);
        if (wasSurface == null) {
            surfaceCache.put(uuid, isSurface);
            return;
        }

        if (wasSurface != isSurface) {
            surfaceCache.put(uuid, isSurface);

            if (isSurface) {
                runAutoCommands(player, config.getSurfaceCommands());
            } else {
                runAutoCommands(player, config.getMiningCommands());
            }
        }
    }

    private void runAutoCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;

        for (String cmd : commands) {
            String parsed = cmd
                    .replace("%player%", player.getName())
                    .replace("%world%", player.getWorld().getName())
                    .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                    .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                    .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));

            String finalCmd = parsed;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            });
        }
    }

    public boolean isSurface(UUID uuid) {
        return surfaceCache.getOrDefault(uuid, false);
    }

    public void setSurface(UUID uuid, boolean surface) {
        surfaceCache.put(uuid, surface);
    }
}
