package com.mauli.levelsystem.listeners;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VoteListener implements Listener {

    private final LevelSystemPlugin plugin;

    public VoteListener(LevelSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Test: Wird angezeigt, wenn der Listener aktiv ist
        event.getPlayer().sendMessage("§aVoteListener aktiv ✅");
    }
}
