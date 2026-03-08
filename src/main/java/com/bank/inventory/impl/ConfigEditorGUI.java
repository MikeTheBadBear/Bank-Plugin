package com.example.bank.inventory.impl;

import com.example.bank.BankPlugin;
import com.example.bank.inventory.InventoryButton;
import com.example.bank.inventory.InventoryGUI;
import com.example.bank.inventory.impl.BankGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ConfigEditorGUI extends InventoryGUI {

    private final com.example.bank.BankPlugin plugin;

    public ConfigEditorGUI(com.example.bank.BankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Config Editor");
    }

    @Override
    public void decorate(Player player) {
        double rate = plugin.getConfigManager().getInterestRate();
        long interval = plugin.getConfigManager().getInterestInterval();

        addButton(10, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("GOLD_INGOT").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.GOLD_INGOT)),
                ChatColor.YELLOW + "Interest Rate: " + String.format("%.2f", rate * 100) + "%",
                Arrays.asList(
                    ChatColor.GRAY + "Current: " + String.format("%.2f", rate * 100) + "%",
                    ChatColor.GRAY + "Click to edit via chat."
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.YELLOW + "Type the new interest rate (e.g., 0.05 for 5%):");
                // Note: Actual chat input handling would require a separate listener, but for simplicity, use command.
                clicker.sendMessage(ChatColor.YELLOW + "Use /bank setinterest <rate> instead.");
            })
        );

        addButton(11, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("GREEN_WOOL").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.GREEN_WOOL)),
                ChatColor.GREEN + "Increase Rate (+0.01)",
                Arrays.asList(ChatColor.GRAY + "Increase interest rate by 0.01")
            ))
            .consumer(event -> {
                double newRate = Math.min(rate + 0.01, 1.0); // Cap at 100%
                plugin.getConfigManager().setInterestRate(newRate);
                decorate(player); // Refresh GUI
            })
        );

        addButton(12, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("RED_WOOL").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.RED_WOOL)),
                ChatColor.RED + "Decrease Rate (-0.01)",
                Arrays.asList(ChatColor.GRAY + "Decrease interest rate by 0.01")
            ))
            .consumer(event -> {
                double newRate = Math.max(rate - 0.01, 0.0); // Min at 0%
                plugin.getConfigManager().setInterestRate(newRate);
                decorate(player); // Refresh GUI
            })
        );

        addButton(14, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("CLOCK").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.CLOCK)),
                ChatColor.AQUA + "Interest Interval: " + interval + "s",
                Arrays.asList(
                    ChatColor.GRAY + "Current: " + interval + " seconds",
                    ChatColor.GRAY + "Click to edit via chat."
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.YELLOW + "Type the new interval in seconds:");
                clicker.sendMessage(ChatColor.YELLOW + "Use /bank setinterval <seconds> (not implemented, use reload).");
            })
        );

        addButton(15, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("GREEN_WOOL").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.GREEN_WOOL)),
                ChatColor.GREEN + "Increase Interval (+60s)",
                Arrays.asList(ChatColor.GRAY + "Increase interval by 60 seconds")
            ))
            .consumer(event -> {
                long newInterval = interval + 60;
                plugin.getConfigManager().setInterestInterval(newInterval);
                decorate(player); // Refresh GUI
            })
        );

        addButton(16, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("RED_WOOL").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.RED_WOOL)),
                ChatColor.RED + "Decrease Interval (-60s)",
                Arrays.asList(ChatColor.GRAY + "Decrease interval by 60 seconds")
            ))
            .consumer(event -> {
                long newInterval = Math.max(interval - 60, 60); // Min 60s
                plugin.getConfigManager().setInterestInterval(newInterval);
                decorate(player); // Refresh GUI
            })
        );

        addButton(22, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("BARRIER").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.BARRIER)),
                ChatColor.RED + "Back",
                Arrays.asList(ChatColor.GRAY + "Return to Bank GUI")
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new BankGUI(plugin), clicker);
            })
        );

        super.decorate(player);
    }

    private ItemStack createItem(ItemStack base, String name, java.util.List<String> lore) {
        ItemStack item = base == null ? new ItemStack(org.bukkit.Material.STONE) : base.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}