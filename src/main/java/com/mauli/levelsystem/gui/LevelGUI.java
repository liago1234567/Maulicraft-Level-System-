package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.store.DataStore;
import com.mauli.levelsystem.hook.VotingHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Level-GUI:
 * - Füllt ALLE Slots mit Level-Kerzen, nur die letzte Reihe wird als Filler genutzt.
 * - Kerzenfarben:
 *      GESPERRT = BLUE_CANDLE
 *      VERFÜGBAR = LIGHT_BLUE_CANDLE (mit Glint/Enchant)
 *      EINGELÖST = LIGHT_GRAY_CANDLE
 * - Stats-Item zeigt Spielzeit & Votes.
 * - Namen/Lore/Bedingungen/Rewars werden dynamisch aus der config.yml gelesen.
 */
public class LevelGUI implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private final LevelManager manager;

    // Caching aus config
    private final String title;
    private final int rows;
    private final int statsSlot;
    private final Material frameMat;

    private final Material matLocked = Material.BLUE_CANDLE;
    private final Material matAvailable = Material.LIGHT_BLUE_CANDLE;
    private final Material matClaimed = Material.LIGHT_GRAY_CANDLE;

    public enum Status { LOCKED, AVAILABLE, CLAIMED }

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin   = plugin;
        this.store    = plugin.getStore();
        this.manager  = plugin.getLevelManager();

        this.title     = color(plugin.getConfig().getString("gui.title", "&fSPIELZEIT LEVEL"));
        this.rows      = Math.max(1, plugin.getConfig().getInt("gui.rows", 6));
        this.statsSlot = Math.max(0, plugin.getConfig().getInt("gui.stats_slot", (rows * 9) - 5));
        this.frameMat  = mat(plugin.getConfig().getString("gui.frame_material", "GRAY_STAINED_GLASS_PANE"));

        // Listener registrieren (falls nicht bereits im Plugin gemacht)
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ------------------------- Public API ------------------------- */

    public void open(Player p) {
        int size = rows * 9;
        // Inventar
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Letzte Reihe als Filler
        ItemStack filler = simple(frameMat, " ");
        int lastRowStart = size - 9;
        for (int i = lastRowStart; i < size; i++) {
            inv.setItem(i, filler);
        }

        // Stats-Item platzieren
        inv.setItem(Math.min(statsSlot, size - 1), buildStatsItem(p));

        // Level-Kerzen in alle übrigen Slots (außer letzte Reihe + evtl. Stats-Slot)
        int levelCount = Math.max(0, store.getLevelCount()); // wie viele Level existieren (über config)
        int placed = 0;
        for (int slot = 0; slot < size - 9 && placed < levelCount; slot++) {
            if (slot == statsSlot) continue; // falls der Stats-Slot nicht in der letzten Reihe liegt
            inv.setItem(slot, buildLevelItem(p, placed + 1));
            placed++;
        }

        p.openInventory(inv);
    }

    /* ------------------------- Item Builder ------------------------- */

    private ItemStack buildStatsItem(Player p) {
        String name = color(plugin.getConfig().getString("stats_item.name", "&f&lSTATISTIKEN"));
        Material mat = mat(plugin.getConfig().getString("stats_item.material", "BOOK"));

        List<String> loreCfg = plugin.getConfig().getStringList("stats_item.lore");
        List<String> lore = new ArrayList<>();

        int minutes = store.getPlaytimeMinutes(p.getUniqueId());
        int votes = VotingHook.getTotalVotes(p); // 0, wenn VotingPlugin nicht aktiv

        for (String line : loreCfg) {
            lore.add(
                    color(
                            line.replace("%playtime%", formatHours(minutes))
                                .replace("%votes%", String.valueOf(votes))
                    )
            );
        }
        if (lore.isEmpty()) {
            lore.add(color("&7Deine Spielzeit: &e" + formatHours(minutes)));
            lore.add(color("&7Deine Votes: &b" + votes));
        }

        return item(mat, name, lore, false);
    }

    private ItemStack buildLevelItem(Player p, int level) {
        Status st = manager.getStatus(p, level);

        String nameAvailable = color(plugin.getConfig().getString("items.available.name", "&bKlicke zum Einlösen"));
        String nameLocked    = color(plugin.getConfig().getString("items.locked.name", "&9Noch nicht freigeschaltet"));
        String nameClaimed   = color(plugin.getConfig().getString("items.claimed.name", "&7Bereits eingelöst"));

        String head = ChatColor.AQUA + "LEVEL " + level;
        List<String> lore = new ArrayList<>();
        lore.add(head);

        int needMin = store.getReqMinutes(level);
        int haveMin = store.getPlaytimeMinutes(p.getUniqueId());
        int needVotes = store.getReqVotes(level);
        int haveVotes = VotingHook.getTotalVotes(p);

        // Stats-Bereich unter dem Level
        lore.add(color("&7Anforderungen:"));
        lore.add(color("&8• &7Spielzeit: &e" + formatHours(haveMin) + " &7/ &e" + formatHours(needMin)));
        lore.add(color("&8• &7Votes: &b" + haveVotes + " &7/ &b" + needVotes));

        // Belohnungen – reine Anzeige (commands bleiben serverseitig konfigurierbar)
        List<String> rewardsDisplay = store.getRewardsDisplay(level);
        if (!rewardsDisplay.isEmpty()) {
            lore.add(color("&7Belohnungen:"));
            for (String r : rewardsDisplay) {
                lore.add(color("&8• &b" + r));
            }
        }

        // Status-spezifische Anzeige
        switch (st) {
            case CLAIMED:
                lore.add(color("&7Du hast diese Belohnung bereits eingelöst"));
                return item(matClaimed, nameClaimed, lore, false);
            case AVAILABLE:
                lore.add(color("&a&lKLICKE ZUM EINLÖSEN!"));
                return item(matAvailable, nameAvailable, lore, true); // Glint
            default:
                lore.add(color("&7Noch nicht freigeschaltet"));
                return item(matLocked, nameLocked, lore, false);
        }
    }

    /* ------------------------- Click Handling ------------------------- */

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        if (e.getView().getTitle() == null || !e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        // Nur Level-Slots (alles außer letzte Reihe und nicht der Stats-Slot)
        int size = rows * 9;
        if (slot >= size - 9) return;
        if (slot == statsSlot) return;

        int index = levelIndexFromSlot(slot);
        int levelCount = store.getLevelCount();
        if (index < 1 || index > levelCount) return;

        if (manager.claim(p, index)) {
            // Erfolgreich eingelöst -> Item aktualisieren
            e.getInventory().setItem(slot, buildLevelItem(p, index));
        } else {
            p.sendMessage(color("&7Dieses Level kann aktuell &cnicht &7eingelöst werden."));
        }
    }

    private int levelIndexFromSlot(int slot) {
        // Wir füllen Slots von 0 .. size-10 linear mit Leveln, Stats-Slot wird übersprungen.
        // Damit die Anzeige stabil bleibt, berechnen wir den „Anzeigeindex“:
        if (slot > statsSlot) return slot; // wenn statsSlot in oberer Sektion liegt
        return slot + 1; // +1 weil Level bei 1 startet
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        // aktuell nichts nötig – Platzhalter, falls später gebraucht
    }

    /* ------------------------- Helpers ------------------------- */

    private ItemStack simple(Material m, String name) {
        return item(m, color(name), null, false);
    }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(color(name));
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
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return Material.GRAY_STAINED_GLASS_PANE;
        }
    }

    private String formatHours(int minutes) {
        // "50 Stunden" Stil – du kannst das bei Bedarf anpassen
        int hrs = Math.max(0, minutes) / 60;
        return hrs + "h";
    }
}
