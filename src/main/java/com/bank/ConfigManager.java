package com.example.bank;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    @Getter
    private double interestRate;
    @Getter
    private long interestInterval;
    @Getter
    private double storageFee;
    @Getter
    private int storageLimit;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        interestRate = config.getDouble("interest-rate", 0.01);
        interestInterval = config.getLong("interest-interval", 3600);
        storageFee = config.getDouble("storage-fee", 10.0);
        storageLimit = config.getInt("storage-limit", 50);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public void setInterestRate(double rate) {
        interestRate = rate;
        plugin.getConfig().set("interest-rate", rate);
        plugin.saveConfig();
    }

    public void setInterestInterval(long interval) {
        interestInterval = interval;
        plugin.getConfig().set("interest-interval", interval);
        plugin.saveConfig();
    }
}