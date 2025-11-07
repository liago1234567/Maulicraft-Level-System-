package com.mauli.levelsystem;

import com.mauli.levelsystem.commands.LevelAdminCommand;
import com.mauli.levelsystem.commands.LevelCommand;
import com.mauli.levelsystem.gui.LevelGUI;
import com.mauli.levelsystem.hook.VotingHook;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.tracker.PlaytimeTracker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystemPlugin extends JavaPlugin {

    private static LevelSystemPlugin instance;

    private DataStore store;
    private LevelManager levelManager;
    private LevelGUI gui;

    public static LevelSystemPlugin getInstance() { return instance; }
    public DataStore getStore() { return store; }
    public LevelManager getLevelManager() { return levelManager; }
    public LevelGUI getGui() { return gui; }

    @Override
    public void onEnable() {
        instance = this;

        // config.yml bei Bedarf erzeugen/laden
        saveDefaultConfig();

        // Kern-Komponenten
        this.store = new DataStore(this);
        this.levelManager = new LevelManager(this);
        this.gui = new LevelGUI(this);

        // Spielzeit-Tracker: jede Minute speichern/prüfen
        new PlaytimeTracker(this).runTaskTimer(this, 20L, 20L * 60);

        // Commands
        if (getCommand("level") != null) {
            getCommand("level").setExecutor(new LevelCommand(this));
        }
        if (getCommand("leveladmin") != null) {
            getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));
        }

        // GUI als Listener registrieren (für Klicks im Inventar)
        Bukkit.getPluginManager().registerEvents(gui, this);

        // VotingPlugin-Hook (optional)
        if (Bukkit.getPluginManager().getPlugin("VotingPlugin") != null) {
            new VotingHook(this); // initialisiert den Hook
            getLogger().info("VotingPlugin gefunden → Vote-Integration aktiv.");
        } else {
            getLogger().warning("VotingPlugin nicht gefunden → Vote-Anforderungen werden ignoriert.");
        }

        getLogger().info("LevelSystem aktiviert.");
    }
}
