package com.example.bank;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerBankData {
    private double balance;
    private List<ItemStack> storedItems;
    private int storedExp;

    public PlayerBankData() {
        this.balance = 0.0;
        this.storedItems = new ArrayList<>();
        this.storedExp = 0;
    }

    public PlayerBankData(double balance) {
        this.balance = balance;
        this.storedItems = new ArrayList<>();
        this.storedExp = 0;
    }

    public PlayerBankData(double balance, List<ItemStack> storedItems) {
        this.balance = balance;
        this.storedItems = storedItems != null ? storedItems : new ArrayList<>();
        this.storedExp = 0;
    }

    public PlayerBankData(double balance, List<ItemStack> storedItems, int storedExp) {
        this.balance = balance;
        this.storedItems = storedItems != null ? storedItems : new ArrayList<>();
        this.storedExp = storedExp;
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public void subtractBalance(double amount) {
        this.balance -= amount;
    }

    public void addStoredExp(int amount) {
        this.storedExp += amount;
    }

    public void subtractStoredExp(int amount) {
        this.storedExp -= amount;
    }
}