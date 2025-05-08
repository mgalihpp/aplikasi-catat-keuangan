package com.example.financemanager;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.financemanager.models.Account;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner accountSpinner;
    private TextView totalIncomeTextView;
    private TextView totalExpenseTextView;
    private TextView netBalanceTextView;
    private LinearLayout categoryBreakdownLayout;

    private List<Account> accountList;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private int selectedAccountId = -1; // -1 indicates all account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize Views
        accountSpinner = findViewById(R.id.reportAccountSpinner);
        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        netBalanceTextView = findViewById(R.id.netBalanceTextView);
        categoryBreakdownLayout = findViewById(R.id.categoryBreakdownLayout);

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Financial Reports");

        // Load account
        this.loadAccounts();

        // Set spinner listener
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // position 0 = "All Accounts"
                if (position == 0) {
                    selectedAccountId = -1;
                } else {
                    selectedAccountId = accountList.get(position - 1).getId();
                }

                // Update report data
                updateReportData();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAccountId = -1;
                updateReportData();
            }
        });
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
     * Load account from database and populate spinner
     */
    private void loadAccounts(){
        accountList = dbHelper.getAllAccounts();

        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("All Accounts");

        for(Account account : accountList){
            spinnerItems.add(account.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(adapter);
    }

    /**
     * Load account from database and populate spinner
     */
    private void updateReportData() {
        double totalIncome, totalExpense, netBalance;
        List<Account> accountList = dbHelper.getAllAccounts();
        if(accountList.isEmpty()){
            currencyFormat.setCurrency(Currency.getInstance("IDR"));
        } else {
            currencyFormat.setCurrency(Currency.getInstance(accountList.get(0).getCurrency()));
        }

        if(selectedAccountId == -1){

            // All accounts
            totalIncome = dbHelper.getTotalIncome();
            totalExpense = dbHelper.getTotalExpense();

        } else {

            totalIncome = dbHelper.getTotalIncome(selectedAccountId);
            totalExpense = dbHelper.getTotalExpense(selectedAccountId);

        }

        netBalance = totalIncome - totalExpense;

        // Update UI
        totalIncomeTextView.setText("Total Income: " + currencyFormat.format(totalIncome));
        totalExpenseTextView.setText("Total Expense: " + currencyFormat.format(totalExpense));
        netBalanceTextView.setText("Net Balance: " + currencyFormat.format(netBalance));

        // Set color for net balance
        if (netBalance < 0) {
            netBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            netBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Update category breakdown
        updateCategoryBreakdown();
    }

    /**
     * Update category breakdown section
     */
    public void updateCategoryBreakdown() {
        // Clear previous views
        categoryBreakdownLayout.removeAllViews();

        // Expense breakdown
        List<DatabaseHelper.CategorySummary> expenseCategories = dbHelper.getExpensesByCategory();
        if (!expenseCategories.isEmpty()) {
            addTitle("Expense Breakdown by Category");
            addCategoryRows(expenseCategories);
        } else {
            addTitle("Expense Breakdown by Category");
            addNoDataMessage("No expense data available");
        }

        // Income breakdown
        List<DatabaseHelper.CategorySummary> incomeCategories = dbHelper.getIncomeByCategory();
        if (!incomeCategories.isEmpty()) {
            addTitle("Income Breakdown by Category");
            addCategoryRows(incomeCategories);
        } else {
            addTitle("Income Breakdown by Category");
            addNoDataMessage("No income data available");
        }
    }

    private void addTitle(String titleText) {
        TextView titleTextView = createTextView(titleText, 18, Typeface.BOLD);
        titleTextView.setPadding(0, dpToPx(20), 0, dpToPx(10));
        categoryBreakdownLayout.addView(titleTextView);
    }

    private void addNoDataMessage(String message) {
        TextView noDataTextView = createTextView(message, 16, Typeface.NORMAL);
        noDataTextView.setGravity(Gravity.CENTER);
        categoryBreakdownLayout.addView(noDataTextView);
    }

    private void addCategoryRows(List<DatabaseHelper.CategorySummary> categories) {
        for (DatabaseHelper.CategorySummary category : categories) {
            LinearLayout row = createCategoryRow(category);
            categoryBreakdownLayout.addView(row);
        }
    }

    private LinearLayout createCategoryRow(DatabaseHelper.CategorySummary category) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dpToPx(2), 0, dpToPx(2));
        row.setLayoutParams(rowParams);

        // Category Name Text
        TextView categoryNameTextView = createTextView(category.getCategory(), 16, Typeface.NORMAL);
        categoryNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Amount Text
        TextView amountTextView = createTextView(currencyFormat.format(category.getAmount()), 16, Typeface.NORMAL);
        amountTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        amountTextView.setGravity(Gravity.END);

        row.addView(categoryNameTextView);
        row.addView(amountTextView);

        return row;
    }


    private TextView createTextView(String text, int textSizeSp, int typeface) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSizeSp);
        textView.setTypeface(null, typeface);
        return textView;
    }

    private int dpToPx(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
