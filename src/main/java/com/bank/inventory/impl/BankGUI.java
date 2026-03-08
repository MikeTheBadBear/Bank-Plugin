package com.example.bank.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.example.bank.BankPlugin;
import com.example.bank.inventory.InventoryButton;
import com.example.bank.inventory.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BankGUI extends InventoryGUI {

    private final com.example.bank.BankPlugin plugin;

    public BankGUI(com.example.bank.BankPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 27, "Bank");
    }
    
    @Override
    public void decorate(Player player) {
        double balance = plugin.getBankManager().getBalance(player.getUniqueId());
        
        this.addButton(11, new InventoryButton()
            .creator(p -> createItem(XMaterial.matchXMaterial("GOLD_INGOT").map(XMaterial::parseItem).orElse(null), "§aDeposit", "§7Click to deposit money"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.sendMessage("Use /bank deposit <amount> to deposit money.");
                clicker.closeInventory();
            })
        );

        this.addButton(13, new InventoryButton()
            .creator(p -> createItem(XMaterial.matchXMaterial("PAPER").map(XMaterial::parseItem).orElse(null), "§bBalance", "§7Your balance: $" + balance))
            .consumer(event -> {})
        );

        this.addButton(15, new InventoryButton()
            .creator(p -> createItem(XMaterial.matchXMaterial("IRON_INGOT").map(XMaterial::parseItem).orElse(null), "§cWithdraw", "§7Click to withdraw money"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.sendMessage("Use /bank withdraw <amount> to withdraw money.");
                clicker.closeInventory();
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createItem(ItemStack base, String name, String lore) {
        ItemStack item = base != null ? base.clone() : new ItemStack(org.bukkit.Material.STONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}