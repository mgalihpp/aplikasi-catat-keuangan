package com.example.financemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.financemanager.models.Account;
import com.example.financemanager.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Database helper class for SQLite operations
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "finance_tracker.db";
    private static final int DATABASE_VERSION = 2;

    // Date formatter
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Table Names
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Account Table Columns
    private static final String ACCOUNT_ID = "id";
    private static final String ACCOUNT_NAME = "name";
    private static final String ACCOUNT_BALANCE = "balance";
    private static final String ACCOUNT_TYPE = "account_type";
    private static final String ACCOUNT_CURRENCY = "currency";
    private static final String ACCOUNT_NOTES = "notes";

    // Transaction Table Columns
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_AMOUNT = "amount";
    private static final String TRANSACTION_TYPE = "type";
    private static final String TRANSACTION_CATEGORY = "category";
    private static final String TRANSACTION_DESCRIPTION = "description";
    private static final String TRANSACTION_DATE = "date";
    private static final String TRANSACTION_ACCOUNT_ID = "account_id";

    // Create Accounts Table Query
    private static final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE " + TABLE_ACCOUNTS + "("
            + ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ACCOUNT_NAME + " TEXT,"
            + ACCOUNT_BALANCE + " REAL,"
            + ACCOUNT_TYPE + " TEXT,"
            + ACCOUNT_CURRENCY + " TEXT,"
            + ACCOUNT_NOTES + " TEXT" + ")";

    // Create Transactions Table Query
    private static final String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TRANSACTION_AMOUNT + " REAL,"
            + TRANSACTION_TYPE + " TEXT,"
            + TRANSACTION_CATEGORY + " TEXT,"
            + TRANSACTION_DESCRIPTION + " TEXT,"
            + TRANSACTION_DATE + " TEXT,"
            + TRANSACTION_ACCOUNT_ID + " INTEGER,"
            + "FOREIGN KEY(" + TRANSACTION_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + ACCOUNT_ID + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNTS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Account CRUD Operations

    /**
     * Add a new account to the database
     * @param account Account object to be added
     * @return The row ID of the newly inserted account
     */
    public long addAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT_NAME, account.getName());
        values.put(ACCOUNT_BALANCE, account.getBalance());
        values.put(ACCOUNT_TYPE, account.getAccountType());
        values.put(ACCOUNT_CURRENCY, account.getCurrency());
        values.put(ACCOUNT_NOTES, account.getNotes());

        // Insert row
        long id = db.insert(TABLE_ACCOUNTS, null, values);
        return id;
    }

    /**
     * Get a single account by ID
     * @param id Account ID
     * @return Account object
     */
    public Account getAccount(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ACCOUNTS,
                new String[] { ACCOUNT_ID, ACCOUNT_NAME, ACCOUNT_BALANCE, ACCOUNT_TYPE, ACCOUNT_CURRENCY, ACCOUNT_NOTES },
                ACCOUNT_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null);

        Account account = null;
        if (cursor != null && cursor.moveToFirst()) {
            account = new Account(
                    cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)),
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_NAME)),
                    cursor.getDouble(cursor.getColumnIndex(ACCOUNT_BALANCE)),
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_TYPE)),
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_CURRENCY)),
                    cursor.getString(cursor.getColumnIndex(ACCOUNT_NOTES))
            );
            cursor.close();
        }
        return account;
    }

    /**
     * Get all accounts from the database
     * @return List of Account objects
     */
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACCOUNTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Account account = new Account(
                        cursor.getInt(cursor.getColumnIndex(ACCOUNT_ID)),
                        cursor.getString(cursor.getColumnIndex(ACCOUNT_NAME)),
                        cursor.getDouble(cursor.getColumnIndex(ACCOUNT_BALANCE)),
                        cursor.getString(cursor.getColumnIndex(ACCOUNT_TYPE)),
                        cursor.getString(cursor.getColumnIndex(ACCOUNT_CURRENCY)),
                        cursor.getString(cursor.getColumnIndex(ACCOUNT_NOTES))
                );
                accounts.add(account);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return accounts;
    }

    /**
     * Update an existing account
     * @param account Account object to update
     * @return Number of rows affected
     */
    public int updateAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT_NAME, account.getName());
        values.put(ACCOUNT_BALANCE, account.getBalance());
        values.put(ACCOUNT_TYPE, account.getAccountType());
        values.put(ACCOUNT_CURRENCY, account.getCurrency());
        values.put(ACCOUNT_NOTES, account.getNotes());

        // Updating row
        int result = db.update(TABLE_ACCOUNTS, values, ACCOUNT_ID + " = ?",
                new String[] { String.valueOf(account.getId()) });
        return result;
    }

    /**
     * Delete an account
     * @param accountId ID of the account to delete
     * @return Number of rows affected
     */
    public int deleteAccount(int accountId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete all transactions related to this account
        db.delete(TABLE_TRANSACTIONS, TRANSACTION_ACCOUNT_ID + " = ?", new String[] { String.valueOf(accountId) });
        // Then delete the account
        int result = db.delete(TABLE_ACCOUNTS, ACCOUNT_ID + " = ?", new String[] { String.valueOf(accountId) });
        return result;
    }

    /**
     * Update account balance
     * @param accountId ID of the account
     * @param amount Amount to adjust (positive for increase, negative for decrease)
     * @return True if successful, false otherwise
     */
    public boolean updateAccountBalance(int accountId, double amount) {
        Account account = getAccount(accountId);
        if (account == null) {
            return false;
        }

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        return updateAccount(account) > 0;
    }

    // Transaction CRUD Operations

    /**
     * Add a new transaction to the database
     * @param transaction Transaction object to be added
     * @return The row ID of the newly inserted transaction
     */
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(TRANSACTION_TYPE, transaction.getType());
        values.put(TRANSACTION_CATEGORY, transaction.getCategory());
        values.put(TRANSACTION_DESCRIPTION, transaction.getDescription());
        values.put(TRANSACTION_DATE, DATE_FORMAT.format(transaction.getDate()));
        values.put(TRANSACTION_ACCOUNT_ID, transaction.getAccountId());

        // Insert row
        long id = db.insert(TABLE_TRANSACTIONS, null, values);

        // Update account balance
        double amountToUpdate = transaction.getType().equals("INCOME")
                ? transaction.getAmount()
                : -transaction.getAmount();

        updateAccountBalance(transaction.getAccountId(), amountToUpdate);

        return id;
    }

    /**
     * Get a single transaction by ID
     * @param id Transaction ID
     * @return Transaction object
     */
    public Transaction getTransaction(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT t.*, a.name as account_name FROM " + TABLE_TRANSACTIONS + " t "
                + "LEFT JOIN " + TABLE_ACCOUNTS + " a ON t.account_id = a.id "
                + "WHERE t.id = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id) });

        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                Date date = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(TRANSACTION_DATE)));

                transaction = new Transaction(
                        cursor.getInt(cursor.getColumnIndex(TRANSACTION_ID)),
                        cursor.getDouble(cursor.getColumnIndex(TRANSACTION_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(TRANSACTION_TYPE)),
                        cursor.getString(cursor.getColumnIndex(TRANSACTION_CATEGORY)),
                        cursor.getString(cursor.getColumnIndex(TRANSACTION_DESCRIPTION)),
                        date,
                        cursor.getInt(cursor.getColumnIndex(TRANSACTION_ACCOUNT_ID)),
                        cursor.getString(cursor.getColumnIndex("account_name"))
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.close();
        }
        return transaction;
    }

    /**
     * Get all transactions for a specific account
     * @param accountId Account ID
     * @return List of Transaction objects
     */
    public List<Transaction> getTransactionsByAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();

        String selectQuery = "SELECT t.*, a.name as account_name FROM " + TABLE_TRANSACTIONS + " t "
                + "LEFT JOIN " + TABLE_ACCOUNTS + " a ON t.account_id = a.id "
                + "WHERE t.account_id = ? "
                + "ORDER BY t.date DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(accountId) });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    Date date = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(TRANSACTION_DATE)));

                    Transaction transaction = new Transaction(
                            cursor.getInt(cursor.getColumnIndex(TRANSACTION_ID)),
                            cursor.getDouble(cursor.getColumnIndex(TRANSACTION_AMOUNT)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_TYPE)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_CATEGORY)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_DESCRIPTION)),
                            date,
                            cursor.getInt(cursor.getColumnIndex(TRANSACTION_ACCOUNT_ID)),
                            cursor.getString(cursor.getColumnIndex("account_name"))
                    );
                    transactions.add(transaction);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }

    /**
     * Get all transactions
     * @return List of Transaction objects
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        String selectQuery = "SELECT t.*, a.name as account_name FROM " + TABLE_TRANSACTIONS + " t "
                + "LEFT JOIN " + TABLE_ACCOUNTS + " a ON t.account_id = a.id "
                + "ORDER BY t.date DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    Date date = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(TRANSACTION_DATE)));

                    Transaction transaction = new Transaction(
                            cursor.getInt(cursor.getColumnIndex(TRANSACTION_ID)),
                            cursor.getDouble(cursor.getColumnIndex(TRANSACTION_AMOUNT)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_TYPE)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_CATEGORY)),
                            cursor.getString(cursor.getColumnIndex(TRANSACTION_DESCRIPTION)),
                            date,
                            cursor.getInt(cursor.getColumnIndex(TRANSACTION_ACCOUNT_ID)),
                            cursor.getString(cursor.getColumnIndex("account_name"))
                    );
                    transactions.add(transaction);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }

    /**
     * Update an existing transaction
     * @param transaction Transaction object to update
     * @return Number of rows affected
     */
    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First get the old transaction to update account balance properly
        Transaction oldTransaction = getTransaction(transaction.getId());

        if (oldTransaction != null) {
            // Reverse the effect of the old transaction
            double reverseAmount = oldTransaction.getType().equals("INCOME")
                    ? -oldTransaction.getAmount()
                    : oldTransaction.getAmount();

            // Apply the new transaction
            double newAmount = transaction.getType().equals("INCOME")
                    ? transaction.getAmount()
                    : -transaction.getAmount();

            // If account changed, update both accounts
            if (oldTransaction.getAccountId() != transaction.getAccountId()) {
                updateAccountBalance(oldTransaction.getAccountId(), reverseAmount);
                updateAccountBalance(transaction.getAccountId(), newAmount);
            } else {
                // Same account, just update the difference
                updateAccountBalance(transaction.getAccountId(), reverseAmount + newAmount);
            }
        }

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(TRANSACTION_TYPE, transaction.getType());
        values.put(TRANSACTION_CATEGORY, transaction.getCategory());
        values.put(TRANSACTION_DESCRIPTION, transaction.getDescription());
        values.put(TRANSACTION_DATE, DATE_FORMAT.format(transaction.getDate()));
        values.put(TRANSACTION_ACCOUNT_ID, transaction.getAccountId());

        // Updating row
        int result = db.update(TABLE_TRANSACTIONS, values, TRANSACTION_ID + " = ?",
                new String[] { String.valueOf(transaction.getId()) });
        return result;
    }

    /**
     * Delete a transaction
     * @param transactionId ID of the transaction to delete
     * @return Number of rows affected
     */
    public int deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First get the transaction to update account balance properly
        Transaction transaction = getTransaction(transactionId);

        if (transaction != null) {
            // Reverse the effect of the transaction
            double reverseAmount = transaction.getType().equals("INCOME")
                    ? -transaction.getAmount()
                    : transaction.getAmount();

            updateAccountBalance(transaction.getAccountId(), reverseAmount);
        }

        // Delete the transaction
        int result = db.delete(TABLE_TRANSACTIONS, TRANSACTION_ID + " = ?",
                new String[] { String.valueOf(transactionId) });
        return result;
    }

    // Report-related methods

    /**
     * Get the total income for a given account
     * @param accountId Account ID
     * @return Total income amount
     */
    public double getTotalIncome(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0;

        String query = "SELECT SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_ACCOUNT_ID + " = ? AND " + TRANSACTION_TYPE + " = 'INCOME'";

        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
        if (cursor != null && cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(cursor.getColumnIndex("total"));
            cursor.close();
        }
        return totalIncome;
    }

    /**
     * Get the total expenses for a given account
     * @param accountId Account ID
     * @return Total expense amount
     */
    public double getTotalExpense(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalExpense = 0;

        String query = "SELECT SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_ACCOUNT_ID + " = ? AND " + TRANSACTION_TYPE + " = 'EXPENSE'";

        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(accountId) });
        if (cursor != null && cursor.moveToFirst()) {
            totalExpense = cursor.getDouble(cursor.getColumnIndex("total"));
            cursor.close();
        }
        return totalExpense;
    }

    /**
     * Get the total income for all accounts
     * @return Total income amount
     */
    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0;

        String query = "SELECT SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_TYPE + " = 'INCOME'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(cursor.getColumnIndex("total"));
            cursor.close();
        }
        return totalIncome;
    }

    /**
     * Get the total expenses for all accounts
     * @return Total expense amount
     */
    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalExpense = 0;

        String query = "SELECT SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_TYPE + " = 'EXPENSE'";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            totalExpense = cursor.getDouble(cursor.getColumnIndex("total"));
            cursor.close();
        }
        return totalExpense;
    }

    /**
     * Get expenses by category
     * @return List of category and amount pairs
     */
    public List<CategorySummary> getExpensesByCategory() {
        List<CategorySummary> categoryExpenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + TRANSACTION_CATEGORY + ", SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_TYPE + " = 'EXPENSE' GROUP BY " + TRANSACTION_CATEGORY +
                " ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndex(TRANSACTION_CATEGORY));
                double amount = cursor.getDouble(cursor.getColumnIndex("total"));
                categoryExpenses.add(new CategorySummary(category, amount));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categoryExpenses;
    }

    /**
     * Get income by category
     * @return List of category and amount pairs
     */
    public List<CategorySummary> getIncomeByCategory() {
        List<CategorySummary> categoryIncomes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + TRANSACTION_CATEGORY + ", SUM(" + TRANSACTION_AMOUNT + ") as total FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANSACTION_TYPE + " = 'INCOME' GROUP BY " + TRANSACTION_CATEGORY +
                " ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndex(TRANSACTION_CATEGORY));
                double amount = cursor.getDouble(cursor.getColumnIndex("total"));
                categoryIncomes.add(new CategorySummary(category, amount));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categoryIncomes;
    }

    /**
     * Inner class for category summary
     */
    public static class CategorySummary {
        private String category;
        private double amount;

        public CategorySummary(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }
    }
}
