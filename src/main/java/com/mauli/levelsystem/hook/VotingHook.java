package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class VotingHook {
    private static boolean available = false;

    public static void init(LevelSystemPlugin plugin) {
        Plugin vp = Bukkit.getPluginManager().getPlugin("VotingPlugin");
        available = (vp != null && vp.isEnabled());
        if (available) {
            plugin.getLogger().info("VotingHook: VotingPlugin erkannt.");
        } else {
            plugin.getLogger().warning("VotingHook: VotingPlugin nicht gefunden – Votes=0.");
        }
    }

    public static int getTotalVotes(Player p) {
        return getTotalVotes(p.getUniqueId());
    }

    public static int getTotalVotes(UUID uuid) {
        if (!available) return 0;
        // TODO: Hier echte VotingPlugin-API nutzen.
        return 0;
    }
}
