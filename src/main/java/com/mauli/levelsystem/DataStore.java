package com.mauli.levelsystem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final LevelSystemPlugin plugin;
    private final File file;
    private final FileConfiguration data;

    public DataStore(LevelSystemPlugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private String path(UUID u, String key){ return "players."+u+"."+key; }

    public int getPlayMinutes(UUID u){ return data.getInt(path(u,"play_minutes"),0); }
    public void addPlayMinute(UUID u){ data.set(path(u,"play_minutes"), getPlayMinutes(u)+1); }

    public int getVotes(UUID u){ return data.getInt(path(u,"votes"),0); }
    public void addVotes(UUID u, int n){ data.set(path(u,"votes"), getVotes(u)+n); }

    public Set<Integer> getClaimed(UUID u){ return new HashSet<>(data.getIntegerList(path(u,"claimed"))); }
    public boolean isClaimed(UUID u, int level){ return getClaimed(u).contains(level); }
    public void setClaimed(UUID u, int level){
        Set<Integer> set = getClaimed(u); set.add(level);
        data.set(path(u,"claimed"), new ArrayList<>(set));
    }
    public void resetClaim(UUID u, int level){
        Set<Integer> set = getClaimed(u); set.remove(level);
        data.set(path(u,"claimed"), new ArrayList<>(set));
    }

    public void saveNow(){
        try { data.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Save failed: " + e.getMessage());
        }
    }
}
