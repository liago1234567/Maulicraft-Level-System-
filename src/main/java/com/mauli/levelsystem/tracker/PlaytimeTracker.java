package com.mauli.levelsystem.tracker;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PlaytimeTracker {
    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private BukkitTask task;

    public PlaytimeTracker(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        // alle 60s + 20 ticks (kleiner Offset) -> Minuten zählen
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                store.addMinute(p.getUniqueId());
            }
        }, 20L*10, 20L*60);
    }

    public void stop() { if (task != null) task.cancel(); }
}
