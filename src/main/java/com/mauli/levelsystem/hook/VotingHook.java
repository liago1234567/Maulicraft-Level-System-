package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class VotingHook implements Listener {

    private final LevelSystemPlugin plugin;

    public VotingHook(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        // Nur registrieren, wenn VotingPlugin wirklich vorhanden ist
        Plugin vp = Bukkit.getPluginManager().getPlugin("VotingPlugin");
        if (vp != null && vp.isEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("VotingHook aktiviert (VotingPlugin gefunden).");
        } else {
            plugin.getLogger().warning("VotingPlugin nicht gefunden – VotingHook bleibt passiv.");
        }
    }
}
