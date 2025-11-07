package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.logic.LevelManager.Status;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelGUI implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private final LevelManager manager;

    // GUI-Konfig
    private final String title;
    private final int rows;
    private final boolean fillerLastRow;
    private final Material fillerMat;

    // Highlight
    private final boolean highlightEnabled;
    private final int highlightLevel;
    private final int highlightSlot;
    private final Material highlightMat;
    private final boolean highlightGlow;
    private final String highlightName;
    private final List<String> highlightLore;

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        this.manager = plugin.getLevelManager();

        // GUI section
        this.title = color(plugin.getConfig().getString("gui.title", "&f&lLEVELS"));
        this.rows = Math.max(1, plugin.getConfig().getInt("gui.rows", 5));
        this.fillerLastRow = plugin.getConfig().getBoolean("gui.filler_last_row", true);
        this.fillerMat = mat(plugin.getConfig().getString("gui.filler_material", "GRAY_STAINED_GLASS_PANE"));

        // highlight section
        ConfigurationSection hl = plugin.getConfig().getConfigurationSection("gui.highlight");
        if (hl != null && hl.getBoolean("enabled", false)) {
            highlightEnabled = true;
            highlightLevel = Math.max(1, hl.getInt("level", 1));
            highlightSlot = Math.max(0, hl.getInt("slot", 13));
            highlightMat = mat(hl.getString("material", "LIGHT_BLUE_CANDLE"));
            highlightGlow = hl.getBoolean("glow", true);
            highlightName = hl.getString("name", "&bFEATURED: Level %level%");
            highlightLore = hl.getStringList("lore");
        } else {
            highlightEnabled = false;
            highlightLevel = 1;
            highlightSlot = 13;
            highlightMat = Material.LIGHT_BLUE_CANDLE;
            highlightGlow = true;
            highlightName = "&bFEATURED: Level %level%";
            highlightLore = new ArrayList<>();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ==================== Öffnen ==================== */

    public void open(Player p) {
        int size = Math.max(9, rows * 9);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Filler nur in der letzten Zeile
        if (fillerLastRow) {
            ItemStack filler = simple(fillerMat, " ");
            int start = size - 9;
            for (int i = start; i < size; i++) inv.setItem(i, filler);
        }

        // Stats-Buch aus config
        inv.setItem(bestStatsSlot(size), buildStatsItem(p));

        int count = Math.max(0, store.getLevelCount());

        // Optional: Highlight-Level an eigenen Slot setzen
        boolean placedHighlight = false;
        if (highlightEnabled && highlightSlot >= 0 && highlightSlot < size && highlightLevel >= 1 && highlightLevel <= count) {
            inv.setItem(highlightSlot, buildLevelItem(p, highlightLevel, true));
            placedHighlight = true;
        }

        // Level-Kerzen von Slot 0 an füllen (überspringe highlightSlot)
        int slot = 0;
        for (int lvl = 1; lvl <= count; lvl++) {
            if (placedHighlight && lvl == highlightLevel) continue; // schon gesetzt
            // letzte Zeile ist bereits Filler → spare dir die 9 Slots unten
            int lastRowStart = size - 9;
            while (slot < size && (slot == highlightSlot || (fillerLastRow && slot >= lastRowStart))) slot++;
            if (slot >= size) break;
            inv.setItem(slot, buildLevelItem(p, lvl, false));
            slot++;
        }

        p.openInventory(inv);
    }

    private int bestStatsSlot(int size) {
        // untere Reihe Mitte, sonst letzter Slot
        int bottomStart = size - 9;
        int mid = bottomStart + 4;
        return (mid >= 0 && mid < size) ? mid : size - 1;
    }

    /* ==================== Items ==================== */

    private ItemStack buildStatsItem(Player p) {
        String name = color(plugin.getConfig().getString("stats_item.name", "&f&lSTATS"));
        Material mat = mat(plugin.getConfig().getString("stats_item.material", "BOOK"));

        int haveMin = store.getPlaytimeMinutes(p.getUniqueId());
        int haveVot = store.getVotes(p.getUniqueId());

        String haveHours = fmtHours(haveMin);

        List<String> lore = new ArrayList<>();
        List<String> lines = plugin.getConfig().getStringList("stats_item.lore");
        if (lines == null || lines.isEmpty()) {
            lore.add(color("&7Spielzeit: &e" + haveHours + " &fh"));
            lore.add(color("&7Votes: &b" + haveVot));
        } else {
            for (String ln : lines) {
                lore.add(color(
                    ln.replace("%have_hours%", haveHours)
                      .replace("%have_minutes%", String.valueOf(haveMin))
                      .replace("%have_votes%", String.valueOf(haveVot))
                ));
            }
        }
        return item(mat, name, lore, false);
    }

    private ItemStack buildLevelItem(Player p, int level, boolean useHighlight) {
        Status st = manager.getStatus(p, level);

        // Anforderungen/Werte
        int needMin = store.getReqMinutes(level);
        int needVot = store.getReqVotes(level);
        int haveMin = store.getPlaytimeMinutes(p.getUniqueId());
        int haveVot = store.getVotes(p.getUniqueId());

        String haveH = fmtHours(haveMin);
        String needH = fmtHours(needMin);

        // Per-level overrides (display)
        String perName = store.getLevelDisplayName(level);      // kann null sein
        List<String> perLore = store.getLevelDisplayLore(level);// kann leer sein

        // Zustand-Items aus config
        ConfigurationSection locked = plugin.getConfig().getConfigurationSection("items.locked");
        ConfigurationSection avail  = plugin.getConfig().getConfigurationSection("items.available");
        ConfigurationSection claimed= plugin.getConfig().getConfigurationSection("items.claimed");

        String name;
        List<String> lore;
        Material mat;
        boolean glow;

        if (useHighlight) {
            // Highlight-Darstellung
            name = color(highlightName.replace("%level%", String.valueOf(level))
                                      .replace("%need_hours%", needH)
                                      .replace("%need_votes%", String.valueOf(needVot)));
            lore = replaceLore(highlightLore, level, haveH, needH, haveMin, needMin, haveVot, needVot);
            mat = highlightMat;
            glow = highlightGlow;
        } else {
            switch (st) {
                case CLAIMED: {
                    name = getStateName(claimed, "&7Bereits eingelöst");
                    lore = getStateLore(claimed);
                    mat  = mat(claimed != null ? claimed.getString("material", "LIGHT_GRAY_CANDLE") : "LIGHT_GRAY_CANDLE");
                    glow = false;
                    break;
                }
                case AVAILABLE: {
                    name = getStateName(avail, "&bKlicke zum Einlösen");
                    lore = getStateLore(avail);
                    mat  = mat(avail != null ? avail.getString("material", "LIGHT_BLUE_CANDLE") : "LIGHT_BLUE_CANDLE");
                    glow = avail != null && avail.getBoolean("glow", true);
                    break;
                }
                default: {
                    name = getStateName(locked, "&9Noch nicht freigeschaltet");
                    lore = getStateLore(locked);
                    mat  = mat(locked != null ? locked.getString("material", "BLUE_CANDLE") : "BLUE_CANDLE");
                    glow = false;
                }
            }
        }

        // Per-level Name/Lore überschreiben (nur wenn konfiguriert)
        if (perName != null && !perName.isEmpty()) name = perName;
        if (perLore != null && !perLore.isEmpty())  lore = perLore;

        // Platzhalter einfügen
        List<String> finalLore = replaceLore(lore, level, haveH, needH, haveMin, needMin, haveVot, needVot);
        String finalName = color(name.replace("%level%", String.valueOf(level)));

        // Zusätzliche Statuszeilen
        switch (st) {
            case CLAIMED: finalLore.add(color("&7Du hast diese Belohnung bereits eingelöst")); break;
            case AVAILABLE: finalLore.add(color("&aKlicke zum Einlösen!")); break;
            default: finalLore.add(color("&7Noch nicht freigeschaltet")); break;
        }

        return item(mat, finalName, finalLore, glow || useHighlight);
    }

    private String getStateName(ConfigurationSection sec, String def) {
        return color(sec != null ? sec.getString("name", def) : def);
    }

    private List<String> getStateLore(ConfigurationSection sec) {
        List<String> l = (sec != null) ? sec.getStringList("lore") : null;
        return (l == null) ? new ArrayList<>() : new ArrayList<>(l);
    }

    private List<String> replaceLore(List<String> lore, int level,
                                     String haveH, String needH,
                                     int haveMin, int needMin, int haveVot, int needVot) {
        List<String> out = new ArrayList<>();
        for (String ln : lore) {
            out.add(color(
                ln.replace("%level%", String.valueOf(level))
                  .replace("%have_hours%", haveH)
                  .replace("%need_hours%", needH)
                  .replace("%have_minutes%", String.valueOf(haveMin))
                  .replace("%need_minutes%", String.valueOf(needMin))
                  .replace("%have_votes%", String.valueOf(haveVot))
                  .replace("%need_votes%", String.valueOf(needVot))
            ));
        }
        return out;
    }

    /* ==================== Click-Handling ==================== */

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        if (e.getView().getTitle() == null || !e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        int size = Math.max(9, rows * 9);
        int lastRowStart = size - 9;

        // Klick auf Filler ignorieren
        if (fillerLastRow && slot >= lastRowStart) return;

        // Klick auf Highlight?
        if (highlightEnabled && slot == highlightSlot) {
            int lvl = highlightLevel;
            if (manager.claim(p, lvl)) {
                e.getInventory().setItem(slot, buildLevelItem(p, lvl, true));
            } else {
                p.sendMessage(color("&7Dieses Level kann aktuell nicht eingelöst werden."));
            }
            return;
        }

        // Sonst: Berechne Level aus Slot (wir haben linear gefüllt und highlight/lastrow übersprungen)
        int count = store.getLevelCount();
        int idx = 0; // wieviele Level bis zu diesem Slot gezählt wurden
        for (int s = 0, lvl = 1; s < size && lvl <= count; s++) {
            if ((fillerLastRow && s >= lastRowStart) || (highlightEnabled && s == highlightSlot)) continue;
            if (s == slot) {
                // Level = lvl
                if (manager.claim(p, lvl)) {
                    e.getInventory().setItem(slot, buildLevelItem(p, lvl, false));
                } else {
                    p.sendMessage(color("&7Dieses Level kann aktuell nicht eingelöst werden."));
                }
                return;
            }
            lvl++;
        }
    }

    @EventHandler public void onClose(InventoryCloseEvent e) {}

    /* ==================== Helpers ==================== */

    private ItemStack simple(Material m, String name) { return item(m, color(name), null, false); }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        if (m == null) m = Material.BARRIER;
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

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
    private Material mat(String key) { try { return Material.valueOf(key.toUpperCase()); } catch (Exception e) { return Material.BARRIER; } }
    private String fmtHours(int minutes) { return String.format("%.1f", minutes / 60.0); }
}
