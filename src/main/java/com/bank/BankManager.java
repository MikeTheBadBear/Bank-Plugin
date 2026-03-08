package com.example.bank;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BankManager {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerBankData> playerData = new HashMap<>();
    private final Path dataFolder;

    public BankManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = Paths.get(plugin.getDataFolder().getPath(), "playerdata");
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create data folder: " + e.getMessage());
        }
        loadAllData();
    }

    public void deposit(Player player, double amount) {
        com.example.bank.BankPlugin bankPlugin = (com.example.bank.BankPlugin) plugin;
        if (bankPlugin.getEconomy().has(player, amount)) {
            bankPlugin.getEconomy().withdrawPlayer(player, amount);
            getPlayerData(player.getUniqueId()).addBalance(amount);
            savePlayerData(player.getUniqueId());
            player.sendMessage("Deposited $" + amount + " into your bank.");
        } else {
            player.sendMessage("You don't have enough money.");
        }
    }

    public void withdraw(Player player, double amount) {
        com.example.bank.BankPlugin bankPlugin = (com.example.bank.BankPlugin) plugin;
        PlayerBankData data = getPlayerData(player.getUniqueId());
        if (data.getBalance() >= amount) {
            data.subtractBalance(amount);
            bankPlugin.getEconomy().depositPlayer(player, amount);
            savePlayerData(player.getUniqueId());
            player.sendMessage("Withdrew $" + amount + " from your bank.");
        } else {
            player.sendMessage("You don't have enough in your bank.");
        }
    }

    public double getBalance(UUID uuid) {
        return getPlayerData(uuid).getBalance();
    }

    public void applyInterest() {
        com.example.bank.BankPlugin bankPlugin = (com.example.bank.BankPlugin) plugin;
        double rate = bankPlugin.getConfigManager().getInterestRate();
        for (PlayerBankData data : playerData.values()) {
            double interest = data.getBalance() * rate;
            data.addBalance(interest);
        }
    }

    public PlayerBankData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> loadPlayerData(uuid));
    }

    private PlayerBankData loadPlayerData(UUID uuid) {
        Path file = dataFolder.resolve(uuid.toString() + ".yml");
        if (Files.exists(file)) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
                double balance = config.getDouble("balance", 0.0);
                List<?> rawItems = config.getList("storedItems");
                List<ItemStack> items = new ArrayList<>();
                if (rawItems != null) {
                    for (Object obj : rawItems) {
                        if (obj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            ItemStack item = ItemStack.deserialize((Map<String, Object>) obj);
                            items.add(item);
                        }
                    }
                }
                int storedExp = config.getInt("storedExp", 0);
                return new PlayerBankData(balance, items, storedExp);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load data for " + uuid + ": " + e.getMessage());
            }
        }
        return new PlayerBankData();
    }

    public void savePlayerData(UUID uuid) {
        Path file = dataFolder.resolve(uuid.toString() + ".yml");
        try {
            YamlConfiguration config = new YamlConfiguration();
            PlayerBankData data = getPlayerData(uuid);
            config.set("balance", data.getBalance());
            List<Map<String, Object>> serializedItems = new ArrayList<>();
            for (ItemStack item : data.getStoredItems()) {
                serializedItems.add(item.serialize());
            }
            config.set("storedItems", serializedItems);
            config.set("storedExp", data.getStoredExp());
            config.save(file.toFile());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAllData() {
        for (UUID uuid : playerData.keySet()) {
            savePlayerData(uuid);
        }
    }

    public boolean depositItem(Player player, ItemStack item) {
        PlayerBankData data = getPlayerData(player.getUniqueId());
        if (data.getStoredItems().size() >= ((com.example.bank.BankPlugin) plugin).getConfigManager().getStorageLimit()) {
            player.sendMessage("Storage is full!");
            return false;
        }
        com.example.bank.BankPlugin bankPlugin = (com.example.bank.BankPlugin) plugin;
        double fee = bankPlugin.getConfigManager().getStorageFee();
        if (!bankPlugin.getEconomy().has(player, fee)) {
            player.sendMessage("You don't have enough money for the storage fee!");
            return false;
        }
        bankPlugin.getEconomy().withdrawPlayer(player, fee);
        data.getStoredItems().add(item.clone());
        savePlayerData(player.getUniqueId());
        player.sendMessage("Item stored for $" + fee);
        return true;
    }

    public boolean withdrawItem(Player player, int index) {
        PlayerBankData data = getPlayerData(player.getUniqueId());
        if (index < 0 || index >= data.getStoredItems().size()) {
            player.sendMessage("Invalid item index!");
            return false;
        }
        ItemStack item = data.getStoredItems().remove(index);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        if (!leftovers.isEmpty()) {
            player.sendMessage("Inventory full, item dropped!");
            player.getWorld().dropItem(player.getLocation(), leftovers.get(0));
        }
        savePlayerData(player.getUniqueId());
        player.sendMessage("Item retrieved!");
        return true;
    }

    public void loadAllData() {
        playerData.clear();
        try {
            Files.list(dataFolder).forEach(path -> {
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(".yml")) {
                    String uuidStr = fileName.substring(0, fileName.length() - 4);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        playerData.put(uuid, loadPlayerData(uuid));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in file: " + fileName);
                    }
                }
            });
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load all data: " + e.getMessage());
        }
    }

    public void depositExp(Player player, int amount) {
        if (amount <= 0) {
            player.sendMessage("Amount must be positive.");
            return;
        }
        if (player.getTotalExperience() < amount) {
            player.sendMessage("You don't have enough experience.");
            return;
        }
        player.giveExp(-amount);
        getPlayerData(player.getUniqueId()).addStoredExp(amount);
        savePlayerData(player.getUniqueId());
        player.sendMessage("Deposited " + amount + " exp into your bank.");
    }

    public void withdrawExp(Player player, int amount) {
        if (amount <= 0) {
            player.sendMessage("Amount must be positive.");
            return;
        }
        PlayerBankData data = getPlayerData(player.getUniqueId());
        if (data.getStoredExp() < amount) {
            player.sendMessage("You don't have enough stored experience.");
            return;
        }
        data.subtractStoredExp(amount);
        player.giveExp(amount);
        savePlayerData(player.getUniqueId());
        player.sendMessage("Withdrew " + amount + " exp from your bank.");
    }

    public int getStoredExp(UUID uuid) {
        return getPlayerData(uuid).getStoredExp();
    }

    public void depositAllExp(Player player) {
        int amount = player.getTotalExperience();
        if (amount <= 0) {
            player.sendMessage("You have no experience to deposit.");
            return;
        }
        player.giveExp(-amount);
        getPlayerData(player.getUniqueId()).addStoredExp(amount);
        savePlayerData(player.getUniqueId());
        player.sendMessage("Deposited all " + amount + " exp into your bank.");
    }
}