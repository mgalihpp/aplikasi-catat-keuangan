package com.example.financemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.financemanager.R;
import com.example.financemanager.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying transaction items in a ListView
 */
public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private final Context context;
    private final List<Transaction> transactions;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public TransactionAdapter(Context context, List<Transaction> transactions, String currency) {
        super(context, R.layout.item_transaction, transactions);
        this.context = context;
        this.transactions = transactions;
        this.currencyFormat.setCurrency(Currency.getInstance(currency));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_transaction, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.transactionAmount = rowView.findViewById(R.id.transactionAmount);
            viewHolder.transactionCategory = rowView.findViewById(R.id.transactionCategory);
            viewHolder.transactionDescription = rowView.findViewById(R.id.transactionDescription);
            viewHolder.transactionDate = rowView.findViewById(R.id.transactionDate);
            viewHolder.transactionAccount = rowView.findViewById(R.id.transactionAccount);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Transaction transaction = transactions.get(position);

        // Format amount with + or - sign
        String formattedAmount;
        if (transaction.isIncome()) {
            formattedAmount = "+ " + currencyFormat.format(transaction.getAmount());
            holder.transactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            formattedAmount = "- " + currencyFormat.format(transaction.getAmount());
            holder.transactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.transactionAmount.setText(formattedAmount);
        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionDescription.setText(transaction.getDescription());

        if (transaction.getDate() != null) {
            holder.transactionDate.setText(dateFormat.format(transaction.getDate()));
        } else {
            holder.transactionDate.setText("");
        }

        holder.transactionAccount.setText(transaction.getAccountName());

        return rowView;
    }

    static class ViewHolder {
        TextView transactionAmount;
        TextView transactionCategory;
        TextView transactionDescription;
        TextView transactionDate;
        TextView transactionAccount;
    }
}
