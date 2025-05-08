package com.example.financemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.financemanager.R;
import com.example.financemanager.models.Account;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends ArrayAdapter<Account> {

    private final Context context;
    private final List<Account> accounts;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public AccountAdapter(@NonNull Context context, List<Account> accounts) {
        super(context, R.layout.item_account, accounts);
        this.context = context;
        this.accounts = accounts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_account, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.accountName = rowView.findViewById(R.id.accountName);
            viewHolder.accountType = rowView.findViewById(R.id.accountType);
            viewHolder.accountBalance = rowView.findViewById(R.id.accountBalance);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Account account = accounts.get(position);

        holder.accountName.setText(account.getName());
        holder.accountType.setText(account.getAccountType());

        // Set currency format based on account currency
        if (account.getCurrency() != null && !account.getCurrency().isEmpty()) {
            try {
                currencyFormat.setCurrency(java.util.Currency.getInstance(account.getCurrency()));
            } catch (IllegalArgumentException e) {
                // If currency code is invalid, fallback to default locale currency
                currencyFormat.setCurrency(java.util.Currency.getInstance(Locale.getDefault()));
            }
        }

        holder.accountBalance.setText(currencyFormat.format(account.getBalance()));

        // Set text color based on balance
        if (account.getBalance() < 0) {
            holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        return rowView;
    }

    static class ViewHolder {
        TextView accountName;
        TextView accountType;
        TextView accountBalance;
    }
}
