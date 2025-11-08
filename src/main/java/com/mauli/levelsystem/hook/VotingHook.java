package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import com.github.nuvotifier.events.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VotingHook implements Listener {

    private final DataStore store;

    public VotingHook(LevelSystemPlugin plugin) {
        this.store = plugin.getStore();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        String username = event.getVote().getUsername();
        Player player = Bukkit.getPlayerExact(username);
        if (player == null) return;

        // Votes erhöhen
        store.setVotes(player.getUniqueId(), store.getVotes(player.getUniqueId()) + 1);
        store.savePlayers();

        // Nachricht an Spieler
        player.sendMessage("§bDanke fürs Voten! §7Deine Votes wurden aktualisiert.");
    }
}
