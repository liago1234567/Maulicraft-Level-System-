package com.mauli.levelsystem.listeners;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVoteEvent;

public class VoteListener implements Listener {

    private final LevelSystemPlugin plugin;

    public VoteListener(LevelSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVote(PlayerVoteEvent event) {
        // +1 Vote hinzufügen
        plugin.addVotes(event.getPlayer().getUniqueId(), 1);
    }
}
