package com.mauli.levelsystem;

import com.mauli.levelsystem.commands.LevelAdminCommand;
import com.mauli.levelsystem.commands.LevelCommand;
import com.mauli.levelsystem.gui.LevelGUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystemPlugin extends JavaPlugin {

    private static LevelSystemPlugin instance;

    public static LevelSystemPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        new LevelGUI(this);
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));

        getLogger().info("LevelSystem aktiviert.");
    }

    @Override
    public void onDisable() {
        getLogger().info("LevelSystem deaktiviert.");
    }
}
