package com.mauli.levelsystem.tracker;

import com.mauli.levelsystem.DataStore;
import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/** Zählt jede Minute 1 Minute Spielzeit für alle Online-Spieler. */
public class PlaytimeTracker extends BukkitRunnable {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public PlaytimeTracker(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store  = plugin.getStore();
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            store.addPlaytimeMinutes(p.getUniqueId(), 1);
        }
    }
}
