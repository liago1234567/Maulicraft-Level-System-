package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.LevelSystemPlugin;
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
            sender.sendMessage("§cDu hast keine Berechtigung dafür.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eBenutzung:");
            sender.sendMessage("§e/leveladmin reload");
            sender.sendMessage("§e/leveladmin open <Spieler>");
            sender.sendMessage("§e/leveladmin addvotes <Spieler> <Zahl>");
            sender.sendMessage("§e/leveladmin resetclaim <Spieler> <Level>");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("§aConfig neu geladen.");
            }

            case "open" -> {
                if (args.length < 2) return true;
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) return true;
                plugin.getGui().open(p, 1);
                sender.sendMessage("§aGUI geöffnet für " + p.getName());
            }

            case "addvotes" -> {
                if (args.length < 3) return true;
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) return true;
                int amount = Integer.parseInt(args[2]);
                plugin.getStore().addVotes(p.getUniqueId(), amount);
                sender.sendMessage("§a" + amount + " Votes hinzugefügt für " + p.getName());
            }

            case "resetclaim" -> {
                if (args.length < 3) return true;
                Player p = Bukkit.getPlayerExact(args[1]);
                if (p == null) return true;
                int level = Integer.parseInt(args[2]);
                plugin.getStore().resetClaim(p.getUniqueId(), level);
                sender.sendMessage("§aClaim für Level " + level + " zurückgesetzt.");
            }
        }

        return true;
    }
}
