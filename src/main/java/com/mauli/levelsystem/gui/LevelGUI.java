package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LevelGUI implements Listener {

    private final LevelSystemPlugin plugin;

    public LevelGUI(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p, int page) {
        Inventory inv = Bukkit.createInventory(p, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title").replace("%page%", String.valueOf(page))));
        p.openInventory(inv);
    }
