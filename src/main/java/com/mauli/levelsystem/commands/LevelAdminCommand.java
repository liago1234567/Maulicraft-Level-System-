package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;

public class LevelAdminCommand implements CommandExecutor {
    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public LevelAdminCommand(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!s.hasPermission("levelsystem.admin")) { s.sendMessage("§cKeine Rechte."); return true; }

        if (a.length == 1 && a[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            s.sendMessage(plugin.getConfig().getString("messages.reloaded","§aConfig neu geladen."));
            return true;
        }

        if (a.length == 3 && (a[0].equalsIgnoreCase("setminutes") || a[0].equalsIgnoreCase("setvotes"))) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(a[1]);
            int num; try { num = Integer.parseInt(a[2]); } catch (Exception e) { s.sendMessage("§cZahl ungültig."); return true; }
            if (a[0].equalsIgnoreCase("setminutes")) store.setMinutes(op.getUniqueId(), num);
            else store.setVotes(op.getUniqueId(), num);
            store.savePlayers();
            s.sendMessage("§aGesetzt für " + op.getName() + ".");
            return true;
        }

        if (a.length == 2 && a[0].equalsIgnoreCase("resetclaims")) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(a[1]);
            store.resetClaims(op.getUniqueId());
            store.savePlayers();
            s.sendMessage("§aClaims von " + op.getName() + " zurückgesetzt.");
            return true;
        }

        s.sendMessage("§e/leveladmin reload");
        s.sendMessage("§e/leveladmin setminutes <Spieler> <Minuten>");
        s.sendMessage("§e/leveladmin setvotes <Spieler> <Zahl>");
        s.sendMessage("§e/leveladmin resetclaims <Spieler>");
        return true;
    }
}
