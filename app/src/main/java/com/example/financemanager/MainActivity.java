package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.financemanager.adapters.AccountAdapter;
import com.example.financemanager.models.Account;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

/**
 * Main activity displaying account overview and providing navigation to other features
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView accountsListView;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private TextView totalBalanceTextView;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        accountsListView = findViewById(R.id.accountsListView);
        totalBalanceTextView = findViewById(R.id.totalBalanceTextView);
        Button addAccountButton = findViewById(R.id.addAccountButton);
        Button addTransactionButton = findViewById(R.id.addTransactionButton);
        Button viewReportsButton = findViewById(R.id.viewReportsButton);
        Button viewHistoryButton = findViewById(R.id.viewHistoryButton);

        // Set click listeners
        addAccountButton.setOnClickListener(v -> openAccountActivity(0)); // 0 means new account

        addTransactionButton.setOnClickListener(v -> {
            if (accountList == null || accountList.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please add an account first", Toast.LENGTH_SHORT).show();
            } else {
                openAddTransactionActivity();
            }
        });

        viewReportsButton.setOnClickListener(v -> openReportActivity());

        viewHistoryButton.setOnClickListener(v -> openTransactionHistoryActivity());

        // Set item click listener for account list
        accountsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Account account = accountList.get(position);
                openAccountActivity(account.getId());
            }
        });

        // Load accounts
        loadAccounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh account list when returning to this activity
        loadAccounts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadAccounts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Load accounts from database and display in ListView
     */
    private void loadAccounts() {
        accountList = dbHelper.getAllAccounts();

        if(accountList.isEmpty()){
            currencyFormat.setCurrency(Currency.getInstance("IDR"));

        } else {
            currencyFormat.setCurrency(Currency.getInstance(accountList.get(0).getCurrency()));
        }

        if (accountList.isEmpty()) {
            // Show a message if no accounts exist
            totalBalanceTextView.setText("No accounts. Add an account to get started.");
        } else {
            // Calculate total balance across all accounts
            double totalBalance = 0;
            for (Account account : accountList) {
                totalBalance += account.getBalance();
            }

            totalBalanceTextView.setText("Total Balance: " + currencyFormat.format(totalBalance));

            // Set text color based on balance
            if (totalBalance < 0) {
                totalBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                totalBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }

        // Update ListView with account data
        accountAdapter = new AccountAdapter(MainActivity.this, accountList);
        accountsListView.setAdapter(accountAdapter);
    }

    /**
     * Open AccountActivity to view, edit or create account
     * @param accountId Account ID (0 for new account)
     */
    private void openAccountActivity(int accountId) {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.putExtra("account_id", accountId);
        startActivity(intent);
    }

    /**
     * Open AddTransactionActivity to create a new transaction
     */
    private void openAddTransactionActivity() {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        startActivity(intent);
    }

    /**
     * Open ReportActivity to view financial reports
     */
    private void openReportActivity() {
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    /**
     * Open TransactionHistoryActivity to view transaction history
     */
    private void openTransactionHistoryActivity() {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        startActivity(intent);
    }
}
