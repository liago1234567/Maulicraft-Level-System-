package com.mauli.levelsystem.logic;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class LevelManager {

    public enum Status { CLAIMED, AVAILABLE, LOCKED }

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public LevelManager(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    public int getLevelCount() { return store.getLevelCount(); }
    public int getReqMinutes(int level) { return store.getReqMinutes(level); }
    public int getReqVotes(int level) { return store.getReqVotes(level); }
    public List<String> getRewards(int level) { return store.getRewards(level); }

    public Status getStatus(Player p, int level) {
        UUID id = p.getUniqueId();
        if (store.isClaimed(id, level)) return Status.CLAIMED;

        int needMin = store.getReqMinutes(level);
        int needVotes = store.getReqVotes(level);
        int haveMin = store.getPlaytimeMinutes(id);
        int haveVotes = store.getVotes(id); // wird von Admin gesetzt oder via Votifier erhöht

        return (haveMin >= needMin && haveVotes >= needVotes) ? Status.AVAILABLE : Status.LOCKED;
    }

    public boolean claim(Player p, int level) {
        if (getStatus(p, level) != Status.AVAILABLE) return false;
        for (String raw : store.getRewards(level)) {
            String cmd = raw.replace("%player%", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
        store.markClaimed(p.getUniqueId(), level);
        store.savePlayers();
        return true;
    }
}
