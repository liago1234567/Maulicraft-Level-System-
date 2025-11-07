package com.mauli.levelsystem;

import com.mauli.levelsystem.commands.LevelCommand;
import com.mauli.levelsystem.gui.LevelGUI;
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

        saveDefaultConfig(); // stellt config.yml bereit
        this.store = new DataStore(this);
        this.levelManager = new LevelManager(this);
        this.gui = new LevelGUI(this);

        // Spielzeit alle 60s hochzählen
        new PlaytimeTracker(this).runTaskTimer(this, 20L, 20L * 60);

        // /level
        getCommand("level").setExecutor(new LevelCommand(this));

        // GUI-Listener
        Bukkit.getPluginManager().registerEvents(gui, this);

        getLogger().info("LevelSystem aktiviert.");
    }
}
