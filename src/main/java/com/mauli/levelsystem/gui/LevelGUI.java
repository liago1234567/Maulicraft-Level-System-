package com.mauli.levelsystem.gui;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
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

    private String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private ItemStack createItem(Material material, String name, List<String> lore){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null){
            meta.setDisplayName(color(name));
            if(lore != null){
                List<String> coloredLore = new ArrayList<>();
                for(String l : lore) coloredLore.add(color(l));
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open(Player p, int page){
        int rows = plugin.getConfig().getInt("gui.rows", 6);
        int size = rows * 9;
        Inventory inv = Bukkit.createInventory(p, size, color(plugin.getConfig().getString("gui.title", "&aLevel Menü").replace("%page%", String.valueOf(page))));

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levels");
        if(section == null) { p.openInventory(inv); return; }

        List<Integer> ids = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            try { ids.add(Integer.parseInt(key)); } catch (Exception ignored) {}
        }
        Collections.sort(ids);

        int minutes = plugin.getStore().getPlayMinutes(p.getUniqueId());
        int votes   = plugin.getStore().getVotes(p.getUniqueId());
        Set<Integer> claimed = plugin.getStore().getClaimed(p.getUniqueId());

        int slot = 0;
        for(int level : ids){
            if(slot >= size) break;

            String base = "levels." + level + ".";
            int reqMin = plugin.getConfig().getInt(base+"required_playtime_minutes",0);
            int reqVotes = plugin.getConfig().getInt(base+"required_votes",0);
            List<String> rewards = plugin.getConfig().getStringList(base+"rewards");

            boolean unlocked = minutes >= reqMin && votes >= reqVotes;
            boolean already = claimed.contains(level);

            List<String> lore = new ArrayList<>();
            lore.add("&7Benötigt: &f"+reqMin+" Min, "+reqVotes+" Votes");
            lore.add("&7Dein Stand: &f"+minutes+" Min, "+votes+" Votes");
            lore.add("&8Belohnungen:");
            for(String r : rewards) lore.add("&8 - &7"+r);

            Material icon;
            String name;

            if(already){
                icon = Material.GRAY_STAINED_GLASS_PANE;
                name = "&7Level "+level+" &8| &7Abgeholt";
            } else if(unlocked){
                icon = Material.CHEST;
                name = "&aLevel "+level+" Belohnung abholen";
                lore.add("&aKlicken!");
            } else {
                icon = Material.BARRIER;
                name = "&cLevel "+level+" gesperrt";
                lore.add("&cNoch nicht erreicht.");
            }

            inv.setItem(slot++, createItem(icon, name, lore));
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p)) return;
        if(!ChatColor.stripColor(e.getView().getTitle()).contains("Level")) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if(clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if(!name.contains("Level")) return;

        try {
            int level = Integer.parseInt(name.replaceAll("\\D+",""));
            if(plugin.getStore().isClaimed(p.getUniqueId(), level)){
                p.sendMessage("§7Belohnung wurde bereits abgeholt.");
                return;
            }

            String base = "levels."+level+".";
            int reqMin = plugin.getConfig().getInt(base+"required_playtime_minutes",0);
            int reqVotes = plugin.getConfig().getInt(base+"required_votes",0);

            if(plugin.getStore().getPlayMinutes(p.getUniqueId()) < reqMin || plugin.getStore().getVotes(p.getUniqueId()) < reqVotes){
                p.sendMessage("§cNoch nicht erreicht.");
                return;
            }

            for(String cmd : plugin.getConfig().getStringList(base+"rewards")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%",p.getName()));
            }
            plugin.getStore().setClaimed(p.getUniqueId(), level);
            plugin.getStore().saveNow();

            p.sendMessage("§aBelohnung für Level "+level+" erhalten!");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            open(p,1);

        } catch (Exception ignored){}
    }
}
