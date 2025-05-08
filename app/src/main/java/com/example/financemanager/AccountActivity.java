package com.example.financemanager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financemanager.adapters.TransactionAdapter;
import com.example.financemanager.models.Account;
import com.example.financemanager.models.Transaction;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

/**
 * Activity for viewing, creating, and editing accounts
 */
public class AccountActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText accountNameEditText;
    private EditText accountInitialBalanceEditText;
    private Spinner accountTypeSpinner;
    private EditText accountCurrencyEditText;
    private EditText accountNotesEditText;
    private Button saveAccountButton;
    private Button deleteAccountButton;
    private TextView accountTransactionsHeaderTextView;
    private ListView transactionsListView;

    private Account currentAccount;
    private int accountId;
    private boolean isNewAccount = true;
    private List<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        accountNameEditText = findViewById(R.id.accountNameEditText);
        accountInitialBalanceEditText = findViewById(R.id.accountInitialBalanceEditText);
        accountTypeSpinner = findViewById(R.id.accountTypeSpinner);
        accountCurrencyEditText = findViewById(R.id.accountCurrencyEditText);
        accountNotesEditText = findViewById(R.id.accountNotesEditText);
        saveAccountButton = findViewById(R.id.saveAccountButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        accountTransactionsHeaderTextView = findViewById(R.id.accountTransactionsHeaderTextView);
        transactionsListView = findViewById(R.id.accountTransactionsListView);

        // Get account ID from intent
        accountId = getIntent().getIntExtra("account_id", 0);
        isNewAccount = (accountId == 0);

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(isNewAccount ? "Create Account" : "Edit Account");

        // Set up view for new or existing account
        setupView();

        // Set up click listeners
        saveAccountButton.setOnClickListener(v -> saveAccount());
        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());

        // Set up transaction list item click listener
        transactionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction transaction = transactionList.get(position);
                // Open edit transaction dialog
                showEditTransactionDialog(transaction);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show menu options for existing accounts
        if (!isNewAccount) {
            getMenuInflater().inflate(R.menu.menu_main, menu);

            MenuItem refreshItem = menu.findItem(R.id.action_refresh);
            refreshItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {

                    loadAccount();
                    loadTransactions();

                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Navigate back
            finish();
            return true;
        } else if (id == R.id.action_refresh) {
            // Refresh account data and transaction list
            if (!isNewAccount) {
                loadAccount();
                loadTransactions();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up view based on whether creating new account or editing existing
     */
    private void setupView() {
        if (isNewAccount) {
            // New account, set default values
            accountCurrencyEditText.setText("USD"); // Default currency
            deleteAccountButton.setVisibility(View.GONE);
            accountTransactionsHeaderTextView.setVisibility(View.GONE);
            transactionsListView.setVisibility(View.GONE);
        } else {
            // Existing account, load data
            loadAccount();
            loadTransactions();
        }
    }

    /**
     * Load account data from database
     */
    private void loadAccount() {
        currentAccount = dbHelper.getAccount(accountId);

        if (currentAccount != null) {
            accountNameEditText.setText(currentAccount.getName());
            accountInitialBalanceEditText.setText(String.valueOf(currentAccount.getBalance()));

            // Set account type in spinner
            String accountType = currentAccount.getAccountType();
            for (int i = 0; i < accountTypeSpinner.getCount(); i++) {
                if (accountTypeSpinner.getItemAtPosition(i).toString().equals(accountType)) {
                    accountTypeSpinner.setSelection(i);
                    break;
                }
            }

            accountCurrencyEditText.setText(currentAccount.getCurrency());
            accountNotesEditText.setText(currentAccount.getNotes());
        }
    }

    /**
     * Load transactions for this account
     */
    private void loadTransactions() {
        transactionList = dbHelper.getTransactionsByAccount(accountId);

        if (transactionList.isEmpty()) {
            accountTransactionsHeaderTextView.setText("No transactions for this account");
        } else {
            accountTransactionsHeaderTextView.setText("Recent Transactions");

            // Set up adapter
            if(transactionAdapter == null){
                transactionAdapter = new TransactionAdapter(this, transactionList, currentAccount.getCurrency());
                transactionsListView.setAdapter(transactionAdapter);
            } else {
                transactionAdapter.clear();
                transactionAdapter.addAll(transactionList);
                transactionAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Save account to database
     */
    private void saveAccount() {
        // Validate input
        String name = accountNameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            accountNameEditText.setError("Please enter an account name");
            return;
        }

        double balance;
        try {
            balance = Double.parseDouble(accountInitialBalanceEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            accountInitialBalanceEditText.setError("Please enter a valid amount");
            return;
        }

        String accountType = accountTypeSpinner.getSelectedItem().toString();
        String currency = accountCurrencyEditText.getText().toString().trim();
        if (currency.isEmpty()) {
            accountCurrencyEditText.setError("Please enter a currency code");
            return;
        }

        String notes = accountNotesEditText.getText().toString().trim();

        if (isNewAccount) {
            // Create new account
            Account newAccount = new Account(name, balance, accountType, currency, notes);
            long result = dbHelper.addAccount(newAccount);

            if (result > 0) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update existing account
            currentAccount.setName(name);
            currentAccount.setBalance(balance);
            currentAccount.setAccountType(accountType);
            currentAccount.setCurrency(currency);
            currentAccount.setNotes(notes);

            int result = dbHelper.updateAccount(currentAccount);

            if (result > 0) {
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                loadAccount(); // Refresh account data
                loadTransactions(); // Refresh transactions
            } else {
                Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Show confirmation dialog before deleting account
     */
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? All related transactions will also be deleted.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete account from database
     */
    private void deleteAccount() {
        int result = dbHelper.deleteAccount(accountId);

        if (result > 0) {
            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show dialog to edit a transaction
     * @param transaction Transaction to edit
     */
    private void showEditTransactionDialog(final Transaction transaction) {
        currencyFormat.setCurrency(Currency.getInstance(currentAccount.getCurrency()));

        // In a real app, you would open a dialog or activity to edit the transaction
        // For this example, we'll just show a simple confirmation dialog to delete the transaction
        new AlertDialog.Builder(this)
                .setTitle("Transaction: " + transaction.getCategory())
                .setMessage("Amount: " + currencyFormat.format(transaction.getAmount()) +
                        "\nDescription: " + transaction.getDescription())
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTransaction(transaction.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete transaction from database
     * @param transactionId ID of transaction to delete
     */
    private void deleteTransaction(int transactionId) {
        int result = dbHelper.deleteTransaction(transactionId);

        if (result > 0) {
            Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
            loadAccount(); // Refresh account data (balance might have changed)
            loadTransactions(); // Refresh transaction list
        } else {
            Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
        }
    }
}
