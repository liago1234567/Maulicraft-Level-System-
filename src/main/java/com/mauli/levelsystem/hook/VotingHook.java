package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Abfrage von Votes – bevorzugt über das Plugin "VotingPlugin".
 * Fallback: interne Speicherung im DataStore (damit funktioniert es auch ohne VotingPlugin).
 */
public final class VotingHook {

    private static boolean checked = false;
    private static boolean votingPluginPresent = false;

    private static void detect() {
        if (checked) return;
        checked = true;
        Plugin pl = Bukkit.getPluginManager().getPlugin("VotingPlugin");
        votingPluginPresent = (pl != null && pl.isEnabled());
    }

    /**
     * Gesamtzahl der Votes für einen Spieler zurückgeben.
     * Versucht zuerst VotingPlugin, fällt sonst auf DataStore zurück.
     */
    public static int getTotalVotes(UUID id) {
        detect();
        if (votingPluginPresent) {
            try {
                // Reflection, damit es ohne harte Abhängigkeit kompiliert.
                // com.bencodez.votingplugin.VotingPluginMain.getInstance().getUserManager().getVotingPluginUser(uuid).getAllTimeTotal()
                Class<?> mainClz = Class.forName("com.bencodez.votingplugin.VotingPluginMain");
                Object main = mainClz.getMethod("getInstance").invoke(null);
                Object userMgr = mainClz.getMethod("getUserManager").invoke(main);
                Object user = userMgr.getClass()
                        .getMethod("getVotingPluginUser", UUID.class)
                        .invoke(userMgr, id);
                Object val = user.getClass().getMethod("getAllTimeTotal").invoke(user);
                return (val instanceof Number) ? ((Number) val).intValue() : 0;
            } catch (Throwable ignored) {
                // Wenn sich API ändert: ruhig auf DataStore zurückfallen
            }
        }
        DataStore store = LevelSystemPlugin.getStore();
        return store.getVotes(id);
    }

    /**
     * Erhöht (fallback) die Votes im DataStore, falls kein VotingPlugin vorhanden ist.
     * Kann z.B. vom /leveladmin addvotes genutzt werden.
     */
    public static void addVoteFallback(UUID id, int amount) {
        DataStore store = LevelSystemPlugin.getStore();
        int curr = store.getVotes(id);
        store.setVotes(id, curr + Math.max(0, amount));
    }

    private VotingHook() {}
}
