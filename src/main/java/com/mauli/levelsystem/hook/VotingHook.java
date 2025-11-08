package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotingHook implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public VotingHook(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        String username = event.getVote().getUsername();
        Player p = Bukkit.getPlayerExact(username);
        if (p == null) return;

        // Vote +1 speichern
        int current = store.getVotes(p.getUniqueId());
        store.setVotes(p.getUniqueId(), current + 1);

        p.sendMessage("§bDanke fürs Voten! §7(§e+" + 1 + " Vote§7)");
    }
}
