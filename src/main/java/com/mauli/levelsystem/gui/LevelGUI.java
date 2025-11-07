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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelGUI implements Listener {

    private final LevelSystemPlugin plugin;
    private final DataStore store;
    private final LevelManager manager;

    // GUI-Einstellungen (statisch, kann bei Bedarf in die config.yml verlegt werden)
    private final String title = ChatColor.DARK_GRAY + "LEVELS";
    private final int rows = 5;                 // 5 Reihen → 45 Slots (0..44)
    private final int statsSlot = 40;           // unten rechts mittig
    private final Material frameMat = Material.GRAY_STAINED_GLASS_PANE;

    // Kerzen je Status
    private final Material matClaimed = Material.LIGHT_GRAY_CANDLE;   // ✅ eingelöst (grau)
    private final Material matAvailable = Material.LIGHT_BLUE_CANDLE; // 🔓 einlösbar (glow)
    private final Material matLocked = Material.BLUE_CANDLE;          // 🔒 gesperrt (dunkelblau)

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        this.manager = plugin.getLevelManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /* ==================== Öffnen ==================== */

    public void open(Player p) {
        int size = Math.max(9, rows * 9);
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Rahmen füllen
        ItemStack filler = simple(frameMat, " ");
        for (int i = 0; i < size; i++) inv.setItem(i, filler);

        // Stats-Buch
        inv.setItem(Math.min(statsSlot, size - 1), buildStatsItem(p));

        // Level-Kerzen 0..(count-1)
        int count = Math.max(0, store.getLevelCount());
        for (int lvl = 1; lvl <= count && (lvl - 1) < size; lvl++) {
            inv.setItem(lvl - 1, buildLevelItem(p, lvl));
        }

        p.openInventory(inv);
    }

    /* ==================== Anzeige-Helfer ==================== */

    // Minuten → "X.Y" h
    private String fmtHours(int minutes) {
        return String.format("%.1f", minutes / 60.0);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private ItemStack simple(Material m, String name) {
        return item(m, color(name), null, false);
    }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        if (m == null) m = Material.BARRIER;
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (glow) {
                // Wichtig: seit MC 1.20+ heißt das Enchantment UNBREAKING (nicht mehr DURABILITY)
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    /* ==================== Items bauen ==================== */

    private ItemStack buildStatsItem(Player p) {
        // Texte können optional aus config.yml gelesen werden; hier einfache Variante
        String name = color(plugin.getConfig().getString("stats_item.name", "&f&lSTATS"));
        Material mat;
        try {
            mat = Material.valueOf(plugin.getConfig().getString("stats_item.material", "BOOK").toUpperCase());
        } catch (Exception e) {
            mat = Material.BOOK;
        }

        int minutes = store.getPlaytimeMinutes(p.getUniqueId());
        int votes   = store.getVotes(p.getUniqueId()); // aktuell aus DataStore; VotingPlugin-Hook kann später integriert werden

        String hoursStr = fmtHours(minutes);

        List<String> lore = new ArrayList<>();
        List<String> cfgLore = plugin.getConfig().getStringList("stats_item.lore");
        if (cfgLore == null || cfgLore.isEmpty()) {
            // Fallback-Lore
            lore.add(color("&7Spielzeit: &e" + hoursStr + " &fh"));
            lore.add(color("&7Votes: &b" + votes));
        } else {
            for (String line : cfgLore) {
                line = line.replace("%playtime%", String.valueOf(minutes)) // falls du Minuten weiterhin irgendwo anzeigen willst
                           .replace("%hours%", hoursStr)
                           .replace("%votes%", String.valueOf(votes));
                lore.add(color(line));
            }
        }

        return item(mat, name, lore, false);
    }

    private ItemStack buildLevelItem(Player p, int level) {
        Status st = manager.getStatus(p, level);

        String nameAvailable = color(plugin.getConfig().getString("items.claim_name", "&aBelohnung abholen"));
        String nameLocked    = color(plugin.getConfig().getString("items.locked_name", "&9Gesperrt"));
        String nameClaimed   = color(plugin.getConfig().getString("items.claimed_name", "&7Bereits abgeholt"));

        String head = ChatColor.AQUA + "LEVEL " + level;
        List<String> lore = new ArrayList<>();
        lore.add(head);

        int needMin = store.getReqMinutes(level);
        int needVot = store.getReqVotes(level);
        int haveMin = store.getPlaytimeMinutes(p.getUniqueId());
        int haveVot = store.getVotes(p.getUniqueId());

        // Anzeige in Stunden
        lore.add(color("&7Spielzeit: &e" + fmtHours(haveMin) + " &7/&e " + fmtHours(needMin) + " &fh"));
        lore.add(color("&7Votes: &b" + haveVot + " &7/&b " + needVot));
        lore.add(color("&7 "));

        List<String> rewards = store.getRewards(level);
        if (!rewards.isEmpty()) {
            lore.add(color("&7Belohnungen:"));
            for (String r : rewards) lore.add(color("&8- &f" + r));
        }

        switch (st) {
            case CLAIMED:
                lore.add(color("&7Du hast diese Belohnung bereits eingelöst"));
                return item(matClaimed, nameClaimed, lore, false);
            case AVAILABLE:
                lore.add(color("&aKlicke zum Einlösen!"));
                return item(matAvailable, nameAvailable, lore, true);
            default:
                lore.add(color("&7Noch nicht freigeschaltet"));
                return item(matLocked, nameLocked, lore, false);
        }
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

        int count = store.getLevelCount();
        if (slot >= 0 && slot < count) {
            int lvl = slot + 1;
            if (manager.claim(p, lvl)) {
                // Item nach dem Claim sofort aktualisieren (grau)
                e.getInventory().setItem(slot, buildLevelItem(p, lvl));
            } else {
                p.sendMessage(color("&7Dieses Level kann aktuell nicht eingelöst werden."));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        // optional: persistente Updates / Sounds / etc.
    }
}
