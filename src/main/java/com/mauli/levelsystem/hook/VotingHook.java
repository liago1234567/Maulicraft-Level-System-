package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import com.bencodez.votingplugin.events.PlayerVoteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class VotingHook implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public VotingHook(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    @EventHandler
    public void onVote(PlayerVoteEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        int cur = store.getVotes(id);
        store.setVotes(id, cur + 1);
    }

    /** Für GUI-Anzeige */
    public static int getTotalVotes(DataStore store, UUID id) {
        return store.getVotes(id);
    }
}
