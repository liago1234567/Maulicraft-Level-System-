package com.mauli.levelsystem.hook;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Votifier-Integration ohne Compile-Time-Abhängigkeit.
 * Wir registrieren das Event via Reflection, damit der Build ohne NuVotifier-API gelingt.
 */
public final class VotingHook implements Listener, EventExecutor {

    private static final String VOTIFIER_EVENT_CLASS = "com.vexsoftware.votifier.model.VotifierEvent";

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public VotingHook(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    /** Versucht das VotifierEvent zu registrieren; gibt true zurück, wenn erfolgreich. */
    public boolean tryRegister() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Event> evt = (Class<? extends Event>) Class.forName(VOTIFIER_EVENT_CLASS);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(evt, this, EventPriority.NORMAL, this, plugin, true);
            plugin.getLogger().info("Votifier erkannt – Votes werden automatisch gezählt.");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Votifier nicht gefunden – Votes bitte per Command setzen.");
            return false;
        }
    }

    /** EventExecutor: wird für ALLE Events aufgerufen, die wir oben registriert haben. */
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!event.getClass().getName().equals(VOTIFIER_EVENT_CLASS)) return;

        try {
            // VotifierEvent#getVote() -> Vote
            Method getVote = event.getClass().getMethod("getVote");
            Object vote = getVote.invoke(event);

            // Vote#getUsername() -> String
            Method getUsername = vote.getClass().getMethod("getUsername");
            String username = (String) getUsername.invoke(vote);

            // Spieler-UUID ermitteln (auch offline möglich)
            OfflinePlayer op = Bukkit.getOfflinePlayer(username);
            UUID id = op.getUniqueId();

            int current = store.getVotes(id);
            store.setVotes(id, current + 1);
            store.savePlayers();

            // Optional: Online-Hinweis
            if (op.isOnline() && op.getPlayer() != null) {
                op.getPlayer().sendMessage("§aDanke fürs Voten! Deine Votes wurden aktualisiert.");
            }
        } catch (Throwable t) {
            throw new EventException(t);
        }
    }
}
