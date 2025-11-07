package com.mauli.levelsystem.commands;

import com.mauli.levelsystem.DataStore;
import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class LevelAdminCommand implements CommandExecutor {

    private final LevelSystemPlugin plugin;
    private final DataStore store;

    public LevelAdminCommand(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store  = plugin.getStore();
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String lbl, String[] a) {
        if (!s.hasPermission("levelsystem.admin")) {
            s.sendMessage("§cDazu hast du keine Rechte.");
            return true;
        }

        if (a.length == 0) {
            help(s);
            return true;
        }

        switch (a[0].toLowerCase()) {

            case "setreq": {
                if (a.length < 4) { s.sendMessage("§7/leveladmin setreq <level> <minutes> <votes>"); return true; }
                int lvl = parseInt(a[1]); int min = parseInt(a[2]); int vot = parseInt(a[3]);
                if (lvl <= 0 || min < 0 || vot < 0) { s.sendMessage("§cUngültige Zahl."); return true; }
                store.setReqMinutes(lvl, min);
                store.setReqVotes(lvl, vot);
                s.sendMessage("§aAnforderungen für Level §b" + lvl + " §agesetzt: §e" + min + " Min, §b" + vot + " Votes.");
                return true;
            }

            case "addreward": {
                if (a.length < 3) { s.sendMessage("§7/leveladmin addreward <level> <konsole-befehl...>"); return true; }
                int lvl = parseInt(a[1]); if (lvl <= 0) { s.sendMessage("§cLevel ungültig."); return true; }
                String cmdToRun = join(a, 2);
                store.addReward(lvl, cmdToRun);
                s.sendMessage("§aReward für Level §b" + lvl + " §ahinzugefügt: §f" + cmdToRun);
                return true;
            }

            case "removereward": {
                if (a.length < 3) { s.sendMessage("§7/leveladmin removereward <level> <index>"); return true; }
                int lvl = parseInt(a[1]); int idx = parseInt(a[2]);
                if (!store.removeReward(lvl, idx)) { s.sendMessage("§cKonnte Reward nicht entfernen (Index prüfen)."); return true; }
                s.sendMessage("§aReward entfernt (Level §b" + lvl + "§a, Index §e" + idx + "§a).");
                return true;
            }

            case "setcount": {
                if (a.length < 2) { s.sendMessage("§7/leveladmin setcount <anzahl>"); return true; }
                int c = parseInt(a[1]); if (c < 0) { s.sendMessage("§cUngültig."); return true; }
                store.setLevelCount(c);
                s.sendMessage("§aLevel-Anzahl auf §e" + c + " §agesetzt.");
                return true;
            }

            case "reset": {
                if (a.length < 2) { s.sendMessage("§7/leveladmin reset <spieler> [level]"); return true; }
                OfflinePlayer op = Bukkit.getOfflinePlayer(a[1]);
                UUID id = op.getUniqueId();
                if (a.length >= 3) {
                    int lvl = parseInt(a[2]);
                    store.setClaimed(id, lvl, false);
                    s.sendMessage("§aClaim für Level §e" + lvl + " §abei §b" + a[1] + " §azurückgesetzt.");
                } else {
                    // alle Claims löschen, indem wir die Datei-Struktur überschreiben
                    // (einzelnes Entfernen aller Level-Keys wäre aufwendiger)
                    for (int i=1;i<=store.getLevelCount();i++) store.setClaimed(id,i,false);
                    s.sendMessage("§aAlle Claims bei §b" + a[1] + " §azurückgesetzt.");
                }
                return true;
            }

            case "reload": {
                plugin.reloadConfig();
                s.sendMessage("§aConfig neu geladen.");
                return true;
            }

            default: help(s); return true;
        }
    }

    private void help(CommandSender s) {
        s.sendMessage("§bLevelSystem Admin-Commands:");
        s.sendMessage("§7/leveladmin setcount <anz>");
        s.sendMessage("§7/leveladmin setreq <level> <minutes> <votes>");
        s.sendMessage("§7/leveladmin addreward <level> <console-cmd mit %player%>");
        s.sendMessage("§7/leveladmin removereward <level> <index>");
        s.sendMessage("§7/leveladmin reset <spieler> [level]");
        s.sendMessage("§7/leveladmin reload");
    }

    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return -1; } }
    private String join(String[] a, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i=from;i<a.length;i++) { if (i>from) sb.append(' '); sb.append(a[i]); }
        return sb.toString();
    }
}
