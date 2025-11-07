package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.DataStore;
import com.mauli.levelsystem.LevelSystemPlugin;
import com.mauli.levelsystem.logic.LevelManager;
import com.mauli.levelsystem.logic.LevelManager.Status;
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
    private final DataStore store;
    private final LevelManager manager;

    private final String title;
    private final int rows;
    private final Material frameMat;
    private final int statsSlot;

    private final Material lockedMat;    // DUNKELBLAU   = gesperrt
    private final Material availMat;     // HELLBLAU + Glint = einlösbar
    private final Material claimedMat;   // HELLGRAU     = eingelöst
    private final boolean availGlow;

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store  = plugin.getStore();
        this.manager= plugin.getLevelManager();

        this.title     = color(plugin.getConfig().getString("gui.title", "&f&lLEVELS"));
        this.rows      = plugin.getConfig().getInt("gui.rows", 6);
        this.frameMat  = mat(plugin.getConfig().getString("gui.filled_item", "GRAY_STAINED_GLASS_PANE"));
        this.statsSlot = plugin.getConfig().getInt("gui.stats_slot", 49);

        this.lockedMat   = mat(plugin.getConfig().getString("items.locked.material", "BLUE_CANDLE"));
        this.availMat    = mat(plugin.getConfig().getString("items.available.material", "LIGHT_BLUE_CANDLE"));
        this.claimedMat  = mat(plugin.getConfig().getString("items.claimed.material", "LIGHT_GRAY_CANDLE"));
        this.availGlow   = plugin.getConfig().getBoolean("items.available.glow", true);
    }

    private Material mat(String name) {
        Material m = Material.matchMaterial(name);
        return m != null ? m : Material.BARRIER;
    }

    public void open(Player p) {
        int size = Math.max(1, rows) * 9;
        Inventory inv = Bukkit.createInventory(null, size, title);

        // Rahmen füllen
        ItemStack filler = simple(frameMat, " ");
        for (int i=0;i<size;i++) inv.setItem(i, filler);

        // Stats Buch
        inv.setItem(Math.min(statsSlot, size-1), buildStatsItem(p));

        // Level Kerzen
        int count = Math.max(0, store.getLevelCount());
        for (int lvl = 1; lvl <= count && lvl-1 < size; lvl++) {
            inv.setItem(lvl-1, buildLevelItem(p, lvl)); // Slots ab 0
        }

        p.openInventory(inv);
    }

    private ItemStack buildStatsItem(Player p) {
        String name = color(plugin.getConfig().getString("stats_item.name", "&f&lSTATS"));
        Material mat = mat(plugin.getConfig().getString("stats_item.material", "BOOK"));

        List<String> loreCfg = plugin.getConfig().getStringList("stats_item.lore");
        List<String> lore = new ArrayList<>();
        int minutes = store.getPlaytimeMinutes(p.getUniqueId());
        int votes   = VotingHook.getTotalVotes(p);

        for (String line : loreCfg) {
            lore.add(color(line.replace("%playtime%", String.valueOf(minutes))
                              .replace("%votes%", String.valueOf(votes))));
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
        int needVot = store.getReqVotes(level);
        int haveVot = VotingHook.getTotalVotes(p);

        lore.add(color("&7Spielzeit: &e" + haveMin + "&7/&e" + needMin + " &7Min."));
        lore.add(color("&7Votes: &b" + haveVot + "&7/&b" + needVot));
        lore.add(color("&7"));

        List<String> rewards = store.getRewards(level);
        if (!rewards.isEmpty()) {
            lore.add(color("&7Belohnungen:"));
            for (String r : rewards) lore.add(color("&8- &f" + r));
        }

        switch (st) {
            case CLAIMED:
                lore.add(color("&cDu hast diese Belohnung bereits eingelöst"));
                return item(claimedMat, nameClaimed, lore, false);
            case AVAILABLE:
                lore.add(color("&aKlicke zum Einlösen"));
                return item(availMat, nameAvailable, lore, availGlow);
            default:
                lore.add(color("&7Noch nicht freigeschaltet"));
                return item(lockedMat, nameLocked, lore, false);
        }
    }

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
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }

    /* ---------------- Click Handling ---------------- */
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
            int level = slot + 1;
            if (manager.claim(p, level)) {
                e.getInventory().setItem(slot, buildLevelItem(p, level));
            } else {
                p.sendMessage("§7Dieses Level kann aktuell nicht eingelöst werden.");
            }
        }
    }
    @EventHandler public void onClose(InventoryCloseEvent e) {}
}
