package com.example.financemanager.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing a financial transaction
 */
public class Transaction {
    private int id;
    private double amount;
    private String type; // "INCOME" or "EXPENSE"
    private String category;
    private String description;
    private Date date;
    private int accountId;
    private String accountName; // For displaying purposes

    // Date formatter
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Default constructor
    public Transaction() {
    }

    // Constructor with all parameters
    public Transaction(int id, double amount, String type, String category,
                       String description, Date date, int accountId, String accountName) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.date = date;
        this.accountId = accountId;
        this.accountName = accountName;
    }

    // Constructor without ID for new transactions
    public Transaction(double amount, String type, String category,
                       String description, Date date, int accountId, String accountName) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.date = date;
        this.accountId = accountId;
        this.accountName = accountName;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    // Utility methods
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }

    public void setDateFromString(String dateString) {
        try {
            this.date = DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            this.date = new Date(); // Current date as fallback
        }
    }

    public boolean isIncome() {
        return "INCOME".equals(type);
    }

    public boolean isExpense() {
        return "EXPENSE".equals(type);
    }

    @Override
    public String toString() {
        return (isIncome() ? "+" : "-") + " " + amount +
                " (" + category + ") - " + description;
    }
}
