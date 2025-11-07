package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.logic.LevelManager.Status;
import com.mauli.levelsystem.tracker.PlaytimeTracker;
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

public class LevelGUI implements Listener {

    private final LevelSystemPlugin plugin;
    private final LevelManager manager;
    private final PlaytimeTracker tracker;

    // Config
    private final String title;
    private final int rows;

    private final Material matClaimed;   // grau
    private final Material matAvailable; // hellblau (glow bei verfügbar)
    private final Material matLocked;    // hellblau (ohne glow)

    private final int statsSlotCfg;      // 1..54
    private final Material statsMat;
    private final String statsName;
    private final List<String> statsLoreCfg;

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getLevelManager();
        this.tracker = plugin.getPlaytimeTracker();

        this.title = color(plugin.getConfig().getString("title", "&7SPIELZEIT LEVEL"));
        this.rows  = Math.max(1, plugin.getConfig().getInt("rows", 6));

        // beide hellblau; Unterscheidung über Glow
        this.matAvailable = mat(plugin.getConfig().getString("materials.available", "LIGHT_BLUE_CANDLE"));
        this.matLocked    = mat(plugin.getConfig().getString("materials.locked", "LIGHT_BLUE_CANDLE"));
        this.matClaimed   = mat(plugin.getConfig().getString("materials.claimed", "LIGHT_GRAY_CANDLE"));

        this.statsSlotCfg = plugin.getConfig().getInt("stats_item.slot", 49);
        this.statsMat     = mat(plugin.getConfig().getString("stats_item.material", "BOOK"));
        this.statsName    = color(plugin.getConfig().getString("stats_item.name", "&bSTATISTIKEN"));
        this.statsLoreCfg = plugin.getConfig().getStringList("stats_item.lore");
    }

    private Material mat(String n) {
        try { return Material.valueOf(n.toUpperCase()); } catch (Exception e) { return Material.LIGHT_BLUE_CANDLE; }
    }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }

    /* ---------- öffnen ---------- */
    public void open(Player p) {
        int size = Math.max(1, rows) * 9;
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Stats setzen (config ist 1..54; intern 0..53)
        int statsSlot = Math.min(Math.max(0, statsSlotCfg - 1), size - 1);
        inv.setItem(statsSlot, buildStatsItem(p));

        // Alle anderen Slots mit Level-Kerzen befüllen (0..size-1, außer statsSlot)
        int levelCount = manager.getLevelCount();
        int placedLevels = 0;

        for (int slot = 0; slot < size && placedLevels < levelCount; slot++) {
            if (slot == statsSlot) continue; // Stats frei lassen
            int level = placedLevels + 1;
            inv.setItem(slot, buildLevelItem(p, level));
            placedLevels++;
        }

        p.openInventory(inv);
    }

    /* ---------- Stats Item ---------- */
    private ItemStack buildStatsItem(Player p) {
        List<String> lore = new ArrayList<>();
        int minutes = tracker.getPlaytimeMinutes(p.getUniqueId());
        int votes   = VotingHook.getTotalVotes(p);

        for (String line : statsLoreCfg) {
            lore.add(color(line
                    .replace("%hours%", String.valueOf(minutes / 60))
                    .replace("%votes%", String.valueOf(votes))));
        }
        return item(statsMat, statsName, lore, false);
    }

    /* ---------- Level Item ---------- */
    private ItemStack buildLevelItem(Player p, int level) {
        Status st = manager.getStatus(p, level);

        String nameAvailable = color(plugin.getConfig().getString("items.claim_name",  "&eKLICKE ZUM EINLÖSEN!"));
        String nameLocked    = color(plugin.getConfig().getString("items.locked_name", "&bNoch nicht freigeschaltet"));
        String nameClaimed   = color(plugin.getConfig().getString("items.claimed_name","&7Bereits eingelöst"));

        String head = ChatColor.AQUA + "LEVEL " + level;
        List<String> lore = new ArrayList<>();
        lore.add(head);

        int needMin   = manager.getReqMinutes(level);
        int needVotes = manager.getReqVotes(level);
        int haveMin   = tracker.getPlaytimeMinutes(p.getUniqueId());
        int haveVotes = VotingHook.getTotalVotes(p);

        lore.add(color("&7ANFORDERUNGEN:"));
        lore.add(color(" &7" + (needMin/60) + " Stunden Spielzeit"));
        lore.add(color(" &7" + needVotes + " Votes"));
        lore.add(color("&7 "));
        lore.add(color("&7BELOHNUNGEN:"));
        List<String> rewards = manager.getRewards(level);
        if (rewards.isEmpty()) lore.add(color(" &8–"));
        else for (String r : rewards) lore.add(color(" &b" + r));
        lore.add(color("&7 "));

        switch (st) {
            case CLAIMED:
                lore.add(nameClaimed);
                return item(matClaimed, nameClaimed, lore, false);
            case AVAILABLE:
                lore.add(nameAvailable);
                return item(matAvailable, nameAvailable, lore, true);  // GLOW = einlösbar
            default:
                lore.add(nameLocked);
                return item(matLocked, nameLocked, lore, false);       // hellblau ohne Glow = gesperrt
        }
    }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(color(name));
            if (lore != null) meta.setLore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true); // 1.21
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    /* ---------- Klick-Handling ---------- */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        if (e.getView().getTitle() == null || !e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);

        int size = rows * 9;
        int statsSlot = Math.min(Math.max(0, statsSlotCfg - 1), size - 1);
        int raw = e.getRawSlot();
        if (raw < 0 || raw >= size || raw == statsSlot) return;

        // Slot → Level (zähle nur Slots vor 'raw', die != statsSlot sind)
        int index = 0;
        for (int s = 0; s < size; s++) {
            if (s == statsSlot) continue;
            if (s == raw) break;
            index++;
        }
        int level = index + 1;
        if (level < 1 || level > manager.getLevelCount()) return;

        if (manager.claim(p, level)) {
            e.getInventory().setItem(raw, buildLevelItem(p, level));
        } else {
            String msg = plugin.getConfig().getString("messages.not_ready",
                    "&cDu hast die Anforderungen für Level %level% noch nicht erfüllt.");
            p.sendMessage(color(msg.replace("%level%", String.valueOf(level))));
        }
    }

    @EventHandler public void onClose(InventoryCloseEvent e) { /* no-op */ }
}
