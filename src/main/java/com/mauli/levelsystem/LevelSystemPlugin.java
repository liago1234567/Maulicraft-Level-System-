package com.mauli.levelsystem;

import com.mauli.levelsystem.hook.VoteListener;
import com.mauli.levelsystem.commands.LevelAdminCommand;
import com.mauli.levelsystem.commands.LevelCommand;
import com.mauli.levelsystem.gui.LevelGUI;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.store.DataStore;
import com.mauli.levelsystem.tracker.PlaytimeTracker;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystemPlugin extends JavaPlugin {

    private static LevelSystemPlugin instance;

    private DataStore store;
    private LevelManager manager;
    private LevelGUI gui;
    private PlaytimeTracker playtimeTracker;

    public static LevelSystemPlugin get() { return instance; }
    public DataStore getStore() { return store; }
    public LevelManager getLevelManager() { return manager; }
    public LevelGUI getGui() { return gui; }
    public PlaytimeTracker getPlaytimeTracker() { return playtimeTracker; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.store = new DataStore(this);
        this.manager = new LevelManager(this);
        this.gui = new LevelGUI(this);
        this.playtimeTracker = new PlaytimeTracker(this);

        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));

        // Optional: Votifier Auto-Vote (nur wenn Plugin installiert ist)
        if (getServer().getPluginManager().getPlugin("Votifier") != null) {
            getServer().getPluginManager().registerEvents(new com.mauli.levelsystem.hook.VoteListener(this), this);
            getLogger().info("Votifier erkannt – Votes werden automatisch gezählt.");
        } else {
            getLogger().info("Votifier nicht gefunden – Votes bitte per Command setzen (oder VotingPlugin-Hook nutzen).");
        }
    }

    @Override
    public void onDisable() {
        if (store != null) store.savePlayers();
        if (playtimeTracker != null) playtimeTracker.stop();
    }
}
