package com.example.bank;

import com.example.bank.inventory.InventoryButton;
import com.example.bank.inventory.InventoryGUI;
import com.example.bank.inventory.impl.ConfigEditorGUI;
import com.example.bank.inventory.impl.ItemStorageGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BankGUI extends InventoryGUI {

    private final com.example.bank.BankPlugin plugin;

    public BankGUI(com.example.bank.BankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Bank");
    }

    @Override
    public void decorate(Player player) {
        double balance = plugin.getBankManager().getBalance(player.getUniqueId());
        double rate = plugin.getConfigManager().getInterestRate() * 100;

        addButton(11, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("GOLD_INGOT").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.GOLD_INGOT)),
                ChatColor.YELLOW + "Deposit",
                Arrays.asList(
                    ChatColor.GRAY + "Click to deposit money",
                    ChatColor.GRAY + "into your bank account."
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/bank deposit <amount>" + ChatColor.YELLOW + " to deposit.");
            })
        );

        addButton(13, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("BOOK").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.BOOK)),
                ChatColor.AQUA + "Balance",
                Arrays.asList(
                    ChatColor.GRAY + "Bank Balance: " + ChatColor.GREEN + "$" + String.format("%.2f", balance),
                    ChatColor.GRAY + "Interest Rate: " + ChatColor.GREEN + String.format("%.2f", rate) + "%"
                )
            ))
            .consumer(event -> {})
        );

        addButton(15, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("EMERALD").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.EMERALD)),
                ChatColor.GREEN + "Withdraw",
                Arrays.asList(
                    ChatColor.GRAY + "Click to withdraw money",
                    ChatColor.GRAY + "from your bank account."
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/bank withdraw <amount>" + ChatColor.YELLOW + " to withdraw.");
            })
        );

        addButton(17, new InventoryButton()
            .creator(p -> createItem(
                XMaterial.matchXMaterial("CHEST").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.CHEST)),
                ChatColor.BLUE + "Item Storage",
                Arrays.asList(
                    ChatColor.GRAY + "Store and retrieve items for a fee."
                )
            ))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getGuiManager().openGUI(new ItemStorageGUI(plugin), clicker);
            })
        );

        if (player.hasPermission("bank.admin")) {
            addButton(22, new InventoryButton()
                .creator(p -> createItem(
                    XMaterial.matchXMaterial("REDSTONE").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.REDSTONE)),
                    ChatColor.RED + "Admin Settings",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to open config editor."
                    )
                ))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.closeInventory();
                    plugin.getGuiManager().openGUI(new com.example.bank.inventory.impl.ConfigEditorGUI(plugin), clicker);
                })
            );
        }

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