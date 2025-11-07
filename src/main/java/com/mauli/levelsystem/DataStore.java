package com.mauli.levelsystem;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Zentrale Speicher-/Konfig-Schicht.
 * - Konfiguration: requirements (minutes/votes), rewards, level count
 * - Daten: playtime (Minuten), claimed Levels
 *
 * Konfig kommt aus config.yml (plugin.getConfig()).
 * Spielerdaten liegen in data.yml in /plugins/LevelSystem/.
 */
public class DataStore {

    private final LevelSystemPlugin plugin;

    // data.yml -> Spielerdaten (Playtime, Claims)
    private File dataFile;
    private FileConfiguration data;

    public DataStore(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        setupDataFile();
    }

/* ---------- Datei-Handling für data.yml ---------- */

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte data.yml nicht erstellen!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte data.yml nicht speichern!");
            e.printStackTrace();
        }
    }

/* ---------- Allgemein ---------- */

    public int getLevelCount() {
        return plugin.getConfig().getInt("levels.count", 0);
    }

    public void setLevelCount(int count) {
        plugin.getConfig().set("levels.count", Math.max(0, count));
        plugin.saveConfig();
    }

    /* ---------- Anforderungen (config.yml) ---------- */

    public int getReqMinutes(int level) {
        return plugin.getConfig().getInt("levels." + level + ".requirements.minutes", 0);
    }

    public void setReqMinutes(int level, int minutes) {
        plugin.getConfig().set("levels." + level + ".requirements.minutes", Math.max(0, minutes));
        plugin.saveConfig();
    }

    public int getReqVotes(int level) {
        return plugin.getConfig().getInt("levels." + level + ".requirements.votes", 0);
    }

    public void setReqVotes(int level, int votes) {
        plugin.getConfig().set("levels." + level + ".requirements.votes", Math.max(0, votes));
        plugin.saveConfig();
    }

    /* ---------- Rewards (config.yml) ---------- */

    public List<String> getRewards(int level) {
        List<String> list = plugin.getConfig().getStringList("levels." + level + ".rewards");
        return list == null ? new ArrayList<>() : list;
    }

    public void addReward(int level, String command) {
        List<String> list = getRewards(level);
        list.add(command);
        plugin.getConfig().set("levels." + level + ".rewards", list);
        plugin.saveConfig();
    }

public boolean removeReward(int level, int index) {
        List<String> list = getRewards(level);
        if (index < 0 || index >= list.size()) return false;
        list.remove(index);
        plugin.getConfig().set("levels." + level + ".rewards", list);
        plugin.saveConfig();
        return true;
    }

    /* ---------- Spielzeit (data.yml, Minuten) ---------- */

    private String minutesPath(UUID uuid) {
        return "players." + uuid.toString() + ".minutes";
    }

    public int getPlaytimeMinutes(UUID uuid) {
        return data.getInt(minutesPath(uuid), 0);
    }

    public void addPlaytimeMinutes(UUID uuid, int delta) {
        int now = getPlaytimeMinutes(uuid);
        data.set(minutesPath(uuid), Math.max(0, now + delta));
        saveData();
    }

    public void setPlaytimeMinutes(UUID uuid, int minutes) {
        data.set(minutesPath(uuid), Math.max(0, minutes));
        saveData();
    }

    /* ---------- Claims (data.yml) ---------- */

    private String claimPath(UUID uuid, int level) {
        return "players." + uuid.toString() + ".claimed." + level;
    }

    public boolean isClaimed(UUID uuid, int level) {
        return data.getBoolean(claimPath(uuid, level), false);
    }

    public void setClaimed(UUID uuid, int level, boolean claimed) {
        data.set(claimPath(uuid, level), claimed);
        saveData();
    }

    /* ---------- Hilfen ---------- */

    public UUID uuidOf(OfflinePlayer p) {
        return p.getUniqueId();
    }
}
