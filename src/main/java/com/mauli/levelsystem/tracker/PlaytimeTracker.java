package com.mauli.levelsystem.tracker;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PlaytimeTracker {
    private final LevelSystemPlugin plugin;
    private BukkitTask task;

    public PlaytimeTracker(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        // alle 60 Sekunden + kleiner Offset
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getStore().addMinute(p.getUniqueId());
            }
        }, 20L * 10, 20L * 60);
    }

    public int getPlaytimeMinutes(java.util.UUID id) {
        return plugin.getStore().getPlaytimeMinutes(id);
    }

    public void stop() { if (task != null) task.cancel(); }
}
