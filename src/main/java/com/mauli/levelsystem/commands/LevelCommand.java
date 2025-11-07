package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final LevelSystemPlugin plugin;

    public LevelCommand(LevelSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl benutzen.");
            return true;
        }

        Player player = (Player) sender;

        // Öffne das neue Level GUI
        plugin.getGui().open(player);

        return true;
    }
}}
