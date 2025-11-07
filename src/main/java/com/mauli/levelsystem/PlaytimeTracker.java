package com.mauli.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlaytimeTracker extends BukkitRunnable {

    private final LevelSystemPlugin plugin;

    public PlaytimeTracker(LevelSystemPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getStore().addPlayMinute(p.getUniqueId());
        }
        plugin.getStore().saveNow();
    }
}
