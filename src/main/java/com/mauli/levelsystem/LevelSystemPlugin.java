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

        saveDefaultConfig(); // config.yml laden/erstellen

        store = new DataStore(this);
        levelManager = new LevelManager(this);
        gui = new LevelGUI(this);

        new PlaytimeTracker(this).runTaskTimer(this, 20L, 20L * 60);

        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));
        Bukkit.getPluginManager().registerEvents(gui, this);

        if (Bukkit.getPluginManager().getPlugin("VotingPlugin") != null) {
            new VotingHook(this);
            getLogger().info("VotingPlugin gefunden → Vote-Integration aktiv.");
        } else {
            getLogger().warning("VotingPlugin NICHT gefunden → Vote-Ziele werden ignoriert.");
        }

        getLogger().info("LevelSystem aktiviert.");
    }
}
