package app.agrivest.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RowAdder {
    Context context;
    RowAdder(Context context) {
        this.context = context;
    }
    public void contract(TableLayout table, LinkedHashMap<String, String> contract) {
        TableRow contractRow = new TableRow(context);
        contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            Button btn = new Button(context);
            TextView textView = new TextView(context);
            String columnValue = contractColumn.getValue().toString();

            if (contractColumn.getKey().equals("amount_pending") || contractColumn.getKey().equals("total_payable") || contractColumn.getKey().equals("total_agreement") || contractColumn.getKey().equals("total_paid")) {
                double amount = Double.parseDouble(contractColumn.getValue().toString());
                NumberFormatter formatter = new NumberFormatter();

                columnValue = formatter.format(amount);
            }

            if (contractColumn.getKey().equals("amount_pending")) {
                textView.setTypeface(null, Typeface.BOLD);
                if (Float.parseFloat(contractColumn.getValue().toString()) > 0) {
                    textView.setTextColor(Color.parseColor("#b03428"));
                } else {
                    textView.setTextColor(Color.parseColor("#196912"));
                }
            }

            if (contractColumn.getKey().equals("id") || contractColumn.getKey().equals("total_payable")) {
                final String id = contract.get("id");
                final String agrivest = contract.get("agrivest");
                final String customer_name = contract.get("customer_name");
                final String customer_address = contract.get("customer_address");
                final String model = contract.get("model");
                final String customer_contact = contract.get("customer_contact");
                final String chassis_number = contract.get("chassis_number");
                final String amount_pending = contract.get("amount_pending");
                final String total_payable = contract.get("total_payable");

                btn.setClickable(true);
                btn.setText(columnValue);
                final String columnKey = contractColumn.getKey().toString();
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = (columnKey.equals("id")) ? new Intent(context, ContractDetailsActivity.class) : new Intent(context, ReceiptActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("id", id);
                        intent.putExtra("agrivest", agrivest);
                        intent.putExtra("customer_name", customer_name);
                        intent.putExtra("customer_address", customer_address);
                        intent.putExtra("model", model);
                        intent.putExtra("customer_contact", customer_contact);
                        intent.putExtra("chassis_number", chassis_number);
                        intent.putExtra("amount_pending", amount_pending);
                        intent.putExtra("total_payable", total_payable);
                        context.startActivity(intent);
                    }
                });
            }

            if (contractColumn.getKey().equals("customer_contact")) {
                textView.setClickable(true);
                textView.setTextColor(Color.parseColor("#3269a8"));
                final String customer_contact = contract.get("customer_contact");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel://" + customer_contact)));
                    }
                });
            }

            textView.setText(columnValue);
            textView.setPadding(10, 10, 10, 10);
            if (contractColumn.getKey().equals("agrivest")) {

            } else {
                if (contractColumn.getKey().equals("id") || contractColumn.getKey().equals("total_payable")) {
                    contractRow.addView(btn);
                } else {
                    contractRow.addView(textView);
                }
            }
        }

        table.addView(contractRow);
    }

    public void receipt(TableLayout table, LinkedHashMap<String, String> contract) {
        TableRow contractRow = new TableRow(context);
        contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            Button btn = new Button(context);
            TextView textView = new TextView(context);
            String columnValue = contractColumn.getValue().toString();

            if (contractColumn.getKey().equals("amount")) {
                double amount = Double.parseDouble(contractColumn.getValue().toString());
                NumberFormatter formatter = new NumberFormatter();

                columnValue = formatter.format(amount);
                textView.setTextColor(Color.parseColor("#0672c9"));
                textView.setTypeface(null, Typeface.BOLD);
            }

            textView.setText(columnValue);
            textView.setPadding(10, 10, 10, 10);
            contractRow.addView(textView);
        }

        table.addView(contractRow);
    }

    public void installment(TableLayout table, LinkedHashMap<String, String> contract) {
        TableRow contractRow = new TableRow(context);
        contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            Button btn = new Button(context);
            TextView textView = new TextView(context);
            String columnValue = contractColumn.getValue().toString();

            if (contractColumn.getKey().equals("installment") || contractColumn.getKey().equals("installment_paid")) {
                double amount = Double.parseDouble(contractColumn.getValue().toString());
                NumberFormatter formatter = new NumberFormatter();

                columnValue = formatter.format(amount);
            }

            if (contractColumn.getKey().equals("installment_paid")) {
                if(contract.get("installment").equals(contract.get("installment_paid"))) {
                    textView.setTextColor(Color.parseColor("#196912"));
                } else {
                    if (Integer.parseInt(contract.get("due_in")) <= 0)
                        textView.setTextColor(Color.parseColor("#b03428"));
                }
                textView.setTypeface(null, Typeface.BOLD);
            }

            textView.setText(columnValue);
            textView.setPadding(10, 10, 10, 10);
            contractRow.addView(textView);
        }

        table.addView(contractRow);
    }
}
