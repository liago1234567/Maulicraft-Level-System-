package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public VoteListener(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    @EventHandler
    public void onVote(VotifierEvent e) {
        String username = e.getVote().getUsername();
        Player p = Bukkit.getPlayerExact(username);

        // Spieler offline → speichern
        if (p == null) {
            store.setVotesOffline(username);
            return;
        }

        // Spieler online → +1 Vote
        store.setVotes(p.getUniqueId(), store.getVotes(p.getUniqueId()) + 1);
        p.sendMessage("§aDanke fürs Voten! §7(§e+1 Vote§7)");
    }
}
