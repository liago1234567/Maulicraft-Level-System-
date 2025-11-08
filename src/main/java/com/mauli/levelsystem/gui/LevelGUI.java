package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.store.DataStore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelGUI implements Listener {

    private static final String TITLE = ChatColor.GRAY + "SPIELZEIT LEVEL";

    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private final LevelManager manager;

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        this.manager = plugin.getLevelManager();
    }

    /** Öffnet das Menü für einen Spieler. */
    public void open(Player p) {
        // 6 Reihen (letzte Reihe ist der Filler-Rahmen / Stats)
        int rows = 6;
        int size = rows * 9;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        // Letzte Reihe mit Rahmen füllen
        ItemStack filler = simple(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = size - 9; i < size; i++) inv.setItem(i, filler);

        // Stats-Item mittig unten (Slot 49 bei 6 Reihen)
        inv.setItem(size - 5, buildStatsItem(p));

        // Level-Items ab Slot 1 (linke obere Ecke) bis vor die letzte Reihe
        int maxPlaceable = size - 9;
        int count = Math.max(0, store.getLevelCount());
        for (int lvl = 1; lvl <= count && (lvl) < maxPlaceable; lvl++) {
            inv.setItem(lvl, buildLevelItem(p, lvl));
        }

        p.openInventory(inv);
    }

    /** Baut das Stats-Item (zeigt Spielzeit & Votes). */
    private ItemStack buildStatsItem(Player p) {
        UUID id = p.getUniqueId();
        int minutes = store.getPlaytimeMinutes(id);
        int votes   = store.getVotes(id);

        List<String> lore = new ArrayList<>();
        lore.add(color("&7DEINE SPIELZEIT: &f" + minutes + "m"));
        lore.add(color("&7DEINE VOTES: &f" + votes));
        lore.add(color("&7 "));

        return item(mat(plugin.getConfig().getString("stats_item.material", "BOOK")),
                    color(plugin.getConfig().getString("stats_item.name", "&f&lSTATS")),
                    lore, false);
    }

    /** Baut ein Level-Item je nach Status (grau = eingelöst, hellblau glüht = einlösbar, dunkelblau = gesperrt). */
    private ItemStack buildLevelItem(Player p, int level) {
        LevelManager.Status st = manager.getStatus(p, level);

        String head = ChatColor.AQUA + "LEVEL " + level;
        List<String> lore = new ArrayList<>();

        int needMin   = store.getReqMinutes(level);
        int needVotes = store.getReqVotes(level);
        UUID id = p.getUniqueId();
        int haveMin   = store.getPlaytimeMinutes(id);
        int haveVotes = store.getVotes(id);

        lore.add(color("&7Spielzeit: &e" + haveMin + " &7/ &e" + needMin + " &7Min."));
        lore.add(color("&7Votes: &b" + haveVotes + " &7/ &b" + needVotes));
        lore.add(color("&7 "));

        Material lockedMat   = Material.BLUE_CANDLE;         // gesperrt (dunkelblau)
        Material availableMat= Material.LIGHT_BLUE_CANDLE;   // verfügbar (hellblau, glüht)
        Material claimedMat  = Material.LIGHT_GRAY_CANDLE;   // eingelöst (grau)

        switch (st) {
            case CLAIMED:
                lore.add(color("&cDu hast diese Belohnung bereits eingelöst"));
                return item(claimedMat, head, lore, false);
            case AVAILABLE:
                lore.add(color("&eKlicke zum Einlösen"));
                return item(availableMat, head, lore, true);
            default:
                lore.add(color("&7Noch nicht freigeschaltet"));
                return item(lockedMat, head, lore, false);
        }
    }

    /** Klick-Handling: auf ein einlösbares Level klicken -> Belohnung ausführen und Item updaten. */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        if (e.getView().getTitle() == null || !e.getView().getTitle().equals(TITLE)) return;

        e.setCancelled(true);
        Player p = (Player) he;

        int slot = e.getRawSlot();
        if (slot < 0) return;

        int count = store.getLevelCount();
        // Unsere Level beginnen bei Slot 1 und gehen bis max Level-Anzahl (vor letzter Reihe)
        if (slot >= 1 && slot <= count) {
            int level = slot;
            if (manager.claim(p, level)) {
                e.getInventory().setItem(slot, buildLevelItem(p, level));
                p.sendMessage(color("&aLevel &e" + level + " &aBelohnung eingelöst!"));
            } else {
                p.sendMessage(color("&7Dieses Level kann aktuell &cnicht &7eingelöst werden."));
            }
        }
    }

    /* ================= Hilfsfunktionen ================= */

    private ItemStack simple(Material m, String name) {
        return item(m, color(name), null, false);
    }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private Material mat(String name) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Material.BOOK; }
    }
}
