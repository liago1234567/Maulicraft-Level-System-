package com.mauli.levelsystem.logic;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.DataStore;
import com.mauli.levelsystem.hook.VotingHook;
import com.mauli.levelsystem.tracker.PlaytimeTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class LevelManager {

    public enum Status { CLAIMED, AVAILABLE, LOCKED }

    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private final PlaytimeTracker tracker;

    public LevelManager(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        this.tracker = plugin.getPlaytime();
    }

    public Status getStatus(Player p, int level) {
        UUID id = p.getUniqueId();
        if (store.isClaimed(id, level)) return Status.CLAIMED;

        int needMin = store.getReqMinutes(level);
        int needVotes = store.getReqVotes(level);
        int haveMin = store.getPlaytimeMinutes(id);
        int haveVotes = VotingHook.getTotalVotes(id);

        return (haveMin >= needMin && haveVotes >= needVotes) ? Status.AVAILABLE : Status.LOCKED;
    }

    public boolean claim(Player p, int level) {
        if (getStatus(p, level) != Status.AVAILABLE) return false;
        List<String> cmds = store.getRewards(level);
        if (cmds != null) {
            for (String raw : cmds) {
                String cmd = raw.replace("%player%", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
        store.markClaimed(p.getUniqueId(), level);
        return true;
    }
}
