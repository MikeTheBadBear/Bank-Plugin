package com.example.bank;

import com.example.bank.citizens.BankerTrait;
import com.example.bank.inventory.gui.GUIListener;
import com.example.bank.inventory.gui.GUIManager;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankPlugin extends JavaPlugin {

    @Getter
    private Economy economy;
    @Getter
    private BankManager bankManager;
    @Getter
    private ConfigManager configManager;
    @Getter
    private GUIManager guiManager;

    private final Map<UUID, PlayerBankData> playerData = new HashMap<>();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        bankManager = new BankManager(this);
        guiManager = new GUIManager();

        getCommand("bank").setExecutor(new BankCommand(this));

        GUIListener guiListener = new GUIListener(guiManager);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BankerTrait.class));
            getLogger().info("Registered BankerTrait for Citizens NPCs.");
        }

        startInterestTask();
    }

    @Override
    public void onDisable() {
        bankManager.saveAllData();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void startInterestTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                bankManager.applyInterest();
            }
        }.runTaskTimer(this, configManager.getInterestInterval() * 20L, configManager.getInterestInterval() * 20L);
    }

    public PlayerBankData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerBankData());
    }

    public void reloadPlugin() {
        configManager.reloadConfig();
        bankManager.loadAllData();
    }
}