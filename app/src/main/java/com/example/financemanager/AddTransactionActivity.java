package com.example.financemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.financemanager.models.Account;
import com.example.financemanager.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText amountEditText;
    private RadioGroup transactionTypeRadioGroup;
    private EditText categoryEditText;
    private EditText descriptionEditText;
    private TextView dateTextView;
    private Spinner accountSpinner;
    private Button saveTransactionButton;

    private Calendar calendar;
    private Date selectedDate;
    private List<Account> accountList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        amountEditText = findViewById(R.id.amountEditText);
        transactionTypeRadioGroup = findViewById(R.id.transactionTypeRadioGroup);
        categoryEditText = findViewById(R.id.categoryEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateTextView = findViewById(R.id.dateTextView);
        accountSpinner = findViewById(R.id.accountSpinner);
        saveTransactionButton = findViewById(R.id.saveTransactionButton);

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Transaction");

        // Initialize date
        calendar = Calendar.getInstance();
        selectedDate = calendar.getTime();
        this.updateDateLabel();

        // Set click listener for date field
        dateTextView.setOnClickListener(v -> this.showDatePicker());

        // Load account list
        this.loadAccounts();

        // Set click listener for save button
        saveTransactionButton.setOnClickListener(v -> this.saveTransaction());
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
     * Update date label text
     */
    private void updateDateLabel(){
        dateTextView.setText(dateFormat.format(selectedDate));
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker(){

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = calendar.getTime();
                        updateDateLabel();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Load accounts from database and populate spinner
     */
    private void loadAccounts(){
        accountList = dbHelper.getAllAccounts();

        if (accountList.isEmpty()){
            Toast.makeText(this, "Please create an account first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<String> accountNames = new ArrayList<>();
        for(Account account: accountList){
            accountNames.add(account.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(adapter);
    }

    /**
     * Save transaction to database
     */
    private void saveTransaction(){
        if(!this.validateInput()){
            return;
        }

        // Get transaction type
        int selectedRadioButtonId = transactionTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String transactionType = selectedRadioButton.getText().toString().toUpperCase();

        // Get amount
        double amount = Double.parseDouble(amountEditText.getText().toString().trim());

        // Get category
        String category = categoryEditText.getText().toString().trim();

        // Get description
        String description = descriptionEditText.getText().toString().trim();

        // Get selected account
        int accountPosition = accountSpinner.getSelectedItemPosition();
        Account selectedAccount = accountList.get(accountPosition);

        // Create transaction object
        Transaction transaction = new Transaction(
                amount,
                transactionType,
                category,
                description,
                selectedDate,
                selectedAccount.getId(),
                selectedAccount.getName()
        );

        // Save result
        long result = dbHelper.addTransaction(transaction);

        if(result > 0){
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate user input
     * @return True if input is valid, false otherwise
     */
    private boolean validateInput(){
        // Validate amount
        String amountStr = amountEditText.getText().toString().trim();
        if(amountStr.isEmpty()){
            amountEditText.setError("Please enter an amount");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0){
                amountEditText.setError("Amount must be positive");
                return false;
            }
        } catch (NumberFormatException e){
            amountEditText.setError("Please enter a valid amount");
            return false;
        }

        // Validate transaction type selection
        if (transactionTypeRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate category
        String category = categoryEditText.getText().toString()
                .trim();
        if(category.isEmpty()){
            categoryEditText.setError("Please enter a category");
            return false;
        }

        // Validate date
        if(selectedDate == null){
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}