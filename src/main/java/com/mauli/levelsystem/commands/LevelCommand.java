package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {
    private final LevelSystemPlugin plugin;
    public LevelCommand(LevelSystemPlugin plugin){ this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player)) { s.sendMessage("Nur für Spieler."); return true; }
        plugin.getGui().open((Player)s);
        return true;
    }
}
