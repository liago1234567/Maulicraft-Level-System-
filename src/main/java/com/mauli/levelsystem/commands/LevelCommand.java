package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.gui.LevelGUI;
import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final LevelGUI gui;

    public LevelCommand(LevelSystemPlugin plugin) {
        this.gui = new LevelGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können das benutzen.");
            return true;
        }
        gui.open(player, 1);
        return true;
    }
}
