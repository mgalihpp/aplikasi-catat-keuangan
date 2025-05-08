package com.example.financemanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.financemanager.adapters.TransactionAdapter;
import com.example.financemanager.models.Transaction;

import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView transactionsListView;
    private TextView noTransactionsTextView;
    private List<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize Views
        transactionsListView = findViewById(R.id.transactionsHistoryListView);
        noTransactionsTextView = findViewById(R.id.noTransactionsTextView);

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Transaction History");

        // Load transaction
        this.loadTransactions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load transactions from database
     */
    private void loadTransactions() {
        transactionList = dbHelper.getAllTransactions();

        if(transactionList.isEmpty()){
            noTransactionsTextView.setText("No transactions found.");
            transactionsListView.setVisibility(View.GONE);
            noTransactionsTextView.setVisibility(View.VISIBLE);
        } else {
            noTransactionsTextView.setVisibility(android.view.View.GONE);
            transactionsListView.setVisibility(android.view.View.VISIBLE);

            // Set up adapter
            transactionAdapter = new TransactionAdapter(this, transactionList, "IDR");
            transactionsListView.setAdapter(transactionAdapter);
        }
    }

}