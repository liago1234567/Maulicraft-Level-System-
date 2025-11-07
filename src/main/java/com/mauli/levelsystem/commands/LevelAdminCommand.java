package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.gui.LevelGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class LevelAdminCommand implements CommandExecutor {

    private final LevelSystemPlugin plugin;

    public LevelAdminCommand(LevelSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("levelsystem.admin")) {
            sender.sendMessage("§cKeine Rechte.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§e/leveladmin reload | addvotes <Spieler> <Zahl> | open <Spieler> | resetclaim <Spieler> <Level>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("§aConfig neu geladen.");
            }
            case "addvotes" -> {
                if (args.length < 3) return false;
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) return true;
                plugin.getConfig().set("players." + p.getUniqueId() + ".votes",
                        plugin.getConfig().getInt("players." + p.getUniqueId() + ".votes") + Integer.parseInt(args[2]));
                plugin.saveConfig();
                sender.sendMessage("§aVotes hinzugefügt.");
            }
            case "open" -> {
                if (args.length < 2) return false;
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) return true;
                new LevelGUI(plugin).open(p, 1);
            }
        }

        return true;
    }
}
