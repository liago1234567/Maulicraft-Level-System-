package com.mauli.levelsystem.logic;

import com.mauli.levelsystem.DataStore;
import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.hook.VotingHook;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/** Prüft Anforderungen, Status und führt beim Klick die Rewards aus. */
public class LevelManager {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public enum Status { LOCKED, AVAILABLE, CLAIMED }

    public LevelManager(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store  = plugin.getStore();
    }

    public int requiredMinutes(int level) { return store.getReqMinutes(level); }
    public int requiredVotes(int level)   { return store.getReqVotes(level);   }

    public int currentMinutes(Player p) { return store.getPlaytimeMinutes(p.getUniqueId()); }
    public int currentVotes(Player p)   { return VotingHook.getTotalVotes(p); }

    /** Status für Kerzenfarbe bestimmen. */
    public Status getStatus(Player p, int level) {
        UUID id = p.getUniqueId();
        if (store.isClaimed(id, level)) return Status.CLAIMED;

        boolean enoughMinutes = currentMinutes(p) >= requiredMinutes(level);
        boolean enoughVotes   = currentVotes(p)   >= requiredVotes(level);

        return (enoughMinutes && enoughVotes) ? Status.AVAILABLE : Status.LOCKED;
    }

    /** Claim ausführen – nur wenn AVAILABLE. */
    public boolean claim(Player p, int level) {
        Status st = getStatus(p, level);
        if (st != Status.AVAILABLE) return false;

        List<String> cmds = store.getRewards(level);
        if (cmds.isEmpty()) {
            p.sendMessage("§cFür dieses Level sind noch keine Belohnungen konfiguriert.");
            return false;
        }

        // Commands mit %player% als Platzhalter ausführen
        for (String cmd : cmds) {
            String exec = cmd.replace("%player%", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), exec);
        }

        store.setClaimed(p.getUniqueId(), level, true);
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.1f);
        p.sendMessage("§aBelohnung für §bLevel " + level + " §aerhalten!");
        return true;
    }
}
