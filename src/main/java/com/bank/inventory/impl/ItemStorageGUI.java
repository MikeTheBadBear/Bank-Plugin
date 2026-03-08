package com.example.bank.inventory.impl;

import com.example.bank.BankPlugin;
import com.example.bank.PlayerBankData;
import com.example.bank.inventory.InventoryButton;
import com.example.bank.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;

public class ItemStorageGUI extends InventoryGUI {

    private final BankPlugin plugin;

    public ItemStorageGUI(BankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Item Storage");
    }

    @Override
    public void decorate(Player player) {
        PlayerBankData data = plugin.getBankManager().getPlayerData(player.getUniqueId());
        List<ItemStack> storedItems = data.getStoredItems();
        int limit = plugin.getConfigManager().getStorageLimit();

        for (int i = 0; i < storedItems.size() && i < 45; i++) {
            final int index = i;
            addButton(i, new InventoryButton()
                .creator(p -> storedItems.get(index))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getBankManager().withdrawItem(clicker, index);
                    decorate(clicker);
                })
            );
        }

        addButton(45, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("CHEST").map(XMaterial::parseItem).orElse(new ItemStack(Material.CHEST)),
                ChatColor.GREEN + "Deposit Item",
                Arrays.asList(
                    ChatColor.GRAY + "Hold an item and click to store",
                    ChatColor.GRAY + "Fee: $" + plugin.getConfigManager().getStorageFee(),
                    ChatColor.GRAY + "Stored: " + storedItems.size() + "/" + limit
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                ItemStack hand = clicker.getInventory().getItemInMainHand();
                if (hand == null || hand.getType() == Material.AIR) {
                    clicker.sendMessage(ChatColor.RED + "Hold an item in your hand to deposit!");
                    return;
                }
                if (plugin.getBankManager().depositItem(clicker, hand)) {
                    clicker.getInventory().setItemInMainHand(null);
                    decorate(clicker);
                }
            })
        );

        addButton(53, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("BARRIER").map(XMaterial::parseItem).orElse(new ItemStack(Material.BARRIER)),
                ChatColor.RED + "Back",
                Arrays.asList(ChatColor.GRAY + "Return to Bank")
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
        ItemStack item = base == null ? new ItemStack(Material.STONE) : base.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}