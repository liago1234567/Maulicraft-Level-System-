package com.mauli.levelsystem.hook;

import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import org.bukkit.entity.Player;

/** Liest Votes direkt aus VotingPlugin (keine eigene Speicherung nötig). */
public class VotingHook {

    public static int getTotalVotes(Player p) {
        VotingPluginUser user = UserManager.getInstance().getVotingPluginUser(p);
        return (user == null) ? 0 : user.getTotalVotes();
    }
}
