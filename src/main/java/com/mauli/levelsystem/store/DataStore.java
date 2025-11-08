 package com.mauli.levelsystem.store;

import com.mauli.levelsystem.LevelSystemPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final LevelSystemPlugin plugin;

    private final File playersFile;
    private final YamlConfiguration playersCfg;

    private final Map<UUID, Integer> minutes = new HashMap<>();
    private final Map<UUID, Integer> votes = new HashMap<>();
    private final Map<UUID, Set<Integer>> claimed = new HashMap<>();

    public DataStore(LevelSystemPlugin plugin) {
        this.plugin = plugin;
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");
        this.playersCfg = YamlConfiguration.loadConfiguration(playersFile);
        loadPlayers();
    }

    /* ----- config getters ----- */
    public int getLevelCount() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("levels");
        return sec == null ? 0 : sec.getKeys(false).size();
    }
    public int getReqMinutes(int level) { return plugin.getConfig().getInt("levels."+level+".required_playtime_minutes", 0); }
    public int getReqVotes(int level) { return plugin.getConfig().getInt("levels."+level+".required_votes", 0); }
    public java.util.List<String> getRewards(int level) { return new ArrayList<>(plugin.getConfig().getStringList("levels."+level+".rewards")); }
    public java.util.List<String> getRewardsDisplay(int level) { return new ArrayList<>(plugin.getConfig().getStringList("levels."+level+".rewards_display")); }

    /* ----- players ----- */
    private void loadPlayers() {
        ConfigurationSection sec = playersCfg.getConfigurationSection("players");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            UUID id = UUID.fromString(key);
            minutes.put(id, playersCfg.getInt("players."+key+".minutes", 0));
            votes.put(id, playersCfg.getInt("players."+key+".votes", 0));
            claimed.put(id, new HashSet<>(playersCfg.getIntegerList("players."+key+".claimed")));
        }
    }
    public void savePlayers() {
        for (UUID id : minutes.keySet()) {
            String p = "players."+id;
            playersCfg.set(p+".minutes", minutes.getOrDefault(id, 0));
            playersCfg.set(p+".votes", votes.getOrDefault(id, 0));
            playersCfg.set(p+".claimed", new ArrayList<>(claimed.getOrDefault(id, Collections.emptySet())));
        }
        try { playersCfg.save(playersFile); } catch (IOException ignored) {}
    }

    public int getPlaytimeMinutes(UUID id) { return minutes.getOrDefault(id, 0); }
    public void addMinute(UUID id) { minutes.put(id, getPlaytimeMinutes(id) + 1); }
    public void setMinutes(UUID id, int m) { minutes.put(id, Math.max(0, m)); }

    public int getVotes(UUID id) { return votes.getOrDefault(id, 0); }
    public void setVotes(UUID id, int v) { votes.put(id, Math.max(0, v)); }

    public boolean isClaimed(UUID id, int level) { return claimed.getOrDefault(id, Collections.emptySet()).contains(level); }
    public void markClaimed(UUID id, int level) { claimed.computeIfAbsent(id, k -> new HashSet<>()).add(level); }
    public void resetClaims(UUID id) { claimed.remove(id); }
}
