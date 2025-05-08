package com.example.financemanager.models;

/**
 * Model class representing a financial account
 */
public class Account {
    private int id;
    private String name;
    private double balance;
    private String accountType; // e.g., "Savings", "Checking", "Cash", etc.
    private String currency;
    private String notes;

    // Default constructor
    public Account() {
    }

    // Constructor with parameters
    public Account(int id, String name, double balance, String accountType, String currency, String notes) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.accountType = accountType;
        this.currency = currency;
        this.notes = notes;
    }

    // Constructor without ID for new accounts
    public Account(String name, double balance, String accountType, String currency, String notes) {
        this.name = name;
        this.balance = balance;
        this.accountType = accountType;
        this.currency = currency;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return name + " (" + accountType + "): " + balance + " " + currency;
    }
}
