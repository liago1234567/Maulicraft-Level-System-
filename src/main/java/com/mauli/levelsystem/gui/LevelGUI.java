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

    private final String title = ChatColor.DARK_GRAY + "LEVELS";
    private final int rows = 5;
    private final int statsSlot = 40;
    private final Material frameMat = Material.GRAY_STAINED_GLASS_PANE;

    private final Material matClaimed = Material.LIGHT_GRAY_CANDLE;    // eingelöst grau
    private final Material matAvailable = Material.LIGHT_BLUE_CANDLE;  // einlösbar, glüht
    private final Material matLocked = Material.BLUE_CANDLE;           // gesperrt dunkelblau

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.store = plugin.getStore();
        this.manager = plugin.getLevelManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        int size = Math.max(9, rows * 9);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ItemStack filler = simple(frameMat, " ");
        for (int i=0;i<size;i++) inv.setItem(i, filler);

        inv.setItem(Math.min(statsSlot, size-1), buildStatsItem(p));

        int count = Math.max(0, store.getLevelCount());
        for (int lvl=1; lvl<=count && (lvl-1)<size; lvl++) {
            inv.setItem(lvl-1, buildLevelItem(p, lvl));
        }
        p.openInventory(inv);
    }

    private ItemStack buildStatsItem(Player p) {
        String name = color(plugin.getConfig().getString("stats_item.name","&f&lSTATS"));
        Material mat = Material.valueOf(plugin.getConfig().getString("stats_item.material","BOOK").toUpperCase());

        int minutes = store.getPlaytimeMinutes(p.getUniqueId());
        int votes = store.getVotes(p.getUniqueId());

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("stats_item.lore")) {
            lore.add(color(line.replace("%playtime%", String.valueOf(minutes))
                               .replace("%votes%", String.valueOf(votes))));
        }
        return item(mat, name, lore, false);
    }

    private ItemStack buildLevelItem(Player p, int level) {
        Status st = manager.getStatus(p, level);

        String nameAvailable = color(plugin.getConfig().getString("items.claim_name","&aBelohnung abholen"));
        String nameLocked    = color(plugin.getConfig().getString("items.locked_name","&9Gesperrt"));
        String nameClaimed   = color(plugin.getConfig().getString("items.claimed_name","&7Bereits abgeholt"));

        String head = ChatColor.AQUA + "LEVEL " + level;
        List<String> lore = new ArrayList<>();
        lore.add(head);

        int needMin = store.getReqMinutes(level);
        int needVot = store.getReqVotes(level);
        int haveMin = store.getPlaytimeMinutes(p.getUniqueId());
        int haveVot = store.getVotes(p.getUniqueId());

        lore.add(color("&7Spielzeit: &e"+haveMin+"&7/&e"+needMin+" &7Min."));
        lore.add(color("&7Votes: &b"+haveVot+"&7/&b"+needVot));
        lore.add(color("&7 "));

        List<String> rewards = store.getRewards(level);
        if (!rewards.isEmpty()) {
            lore.add(color("&7Belohnungen:"));
            for (String r : rewards) lore.add(color("&8- &f"+r));
        }

        switch (st) {
            case CLAIMED:   lore.add(color("&7Du hast diese Belohnung bereits eingelöst"));
                            return item(matClaimed, nameClaimed, lore, false);
            case AVAILABLE: lore.add(color("&aKlicke zum Einlösen!"));
                            return item(matAvailable, nameAvailable, lore, true);
            default:        lore.add(color("&7Noch nicht freigeschaltet"));
                            return item(matLocked, nameLocked, lore, false);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        if (e.getView().getTitle()==null || !e.getView().getTitle().equals(title)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;

        int count = store.getLevelCount();
        if (slot >= 0 && slot < count) {
            int lvl = slot + 1;
            if (manager.claim(p, lvl)) {
                e.getInventory().setItem(slot, buildLevelItem(p, lvl));
            } else {
                p.sendMessage(color("&7Dieses Level kann aktuell nicht eingelöst werden."));
            }
        }
    }

    @EventHandler public void onClose(InventoryCloseEvent e) {}

    private ItemStack simple(Material m, String name) { return item(m, color(name), null, false); }

    private ItemStack item(Material m, String name, List<String> lore, boolean glow) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            it.setItemMeta(meta);
        }
        return it;
    }

    private String color(String s){ return ChatColor.translateAlternateColorCodes('&', s==null?"":s); }
}
