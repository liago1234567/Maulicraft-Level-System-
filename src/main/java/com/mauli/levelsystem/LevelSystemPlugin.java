package com.mauli.levelsystem;

import com.mauli.levelsystem.commands.LevelAdminCommand;
import com.mauli.levelsystem.commands.LevelCommand;
import com.mauli.levelsystem.gui.LevelGUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystemPlugin extends JavaPlugin {

    private static LevelSystemPlugin instance;
    private DataStore store;
    private PlaytimeTracker playtime;
    private LevelGUI gui;

    public static LevelSystemPlugin getInstance() { return instance; }
    public DataStore getStore() { return store; }
    public LevelGUI getGui() { return gui; }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();              // plugin.yml/config.yml bereitstellen
        store = new DataStore(this);      // Datenverwaltung
        playtime = new PlaytimeTracker(this);
        playtime.runTaskTimer(this, 20L, 20L * 60); // jede Minute

        gui = new LevelGUI(this);

        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));

        // Optional: VoteListener registrieren (nur nötig, wenn du später NuVotifier nutzt)

        getLogger().info("LevelSystem aktiviert.");
    }

    @Override
    public void onDisable() {
        store.saveNow();
        getLogger().info("LevelSystem deaktiviert.");
    }
}
