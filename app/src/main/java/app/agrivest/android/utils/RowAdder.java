package app.agrivest.android.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import app.agrivest.android.R;
import app.agrivest.android.activities.ContractDetailsActivity;
import app.agrivest.android.activities.ReceiptActivity;

public class RowAdder {
    Context context;
    public RowAdder(Context context) {
        this.context = context;
    }
    public void contract(TableLayout table, LinkedHashMap<String, String> contract) {
        TableRow contractRow = new TableRow(context);
        contractRow.setBackgroundColor(Color.parseColor("#f2eded"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            MaterialButton btn = new MaterialButton(context, null, R.attr.materialButtonOutlinedStyle);
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

                btn.setClickable(true);
                btn.setText(columnValue);
                final String columnKey = contractColumn.getKey().toString();
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = (columnKey.equals("id")) ? new Intent(context, ContractDetailsActivity.class) : new Intent(context, ReceiptActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("id", id);
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
        contractRow.setBackgroundColor(Color.parseColor("#f2eded"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
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
        contractRow.setBackgroundColor(Color.parseColor("#f2eded"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
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

    public void commitment(TableLayout table, LinkedHashMap<String, String> contract, String type) {
        TableRow contractRow = new TableRow(context);
        contractRow.setBackgroundColor(Color.parseColor("#f2eded"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            MaterialButton btn = new MaterialButton(context, null, R.attr.materialButtonOutlinedStyle);
            TextView textView = new TextView(context);
            String columnValue = contractColumn.getValue().toString();
            if (contractColumn.getKey().equals("installment") || contractColumn.getKey().equals("installment_paid")) {
                double amount = Double.parseDouble(contractColumn.getValue().toString());
                NumberFormatter formatter = new NumberFormatter();

                columnValue = formatter.format(amount);
            }

            textView.setText(columnValue);

            if (contractColumn.getKey().equals("contract_id") || contractColumn.getKey().equals("total_payable")) {
                final String id = contract.get("contract_id");

                btn.setClickable(true);
                btn.setText(columnValue);
                final String columnKey = contractColumn.getKey().toString();
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = (columnKey.equals("contract_id")) ? new Intent(context, ContractDetailsActivity.class) : new Intent(context, ReceiptActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("id", id);
                        context.startActivity(intent);
                    }
                });
            }

            if (contractColumn.getKey().equals("due_in")) {
                if(type.equals("upcoming")) {
                    textView.setText("Expires in " + Math.abs(Integer.parseInt(columnValue)) + " days");
                    textView.setTextColor(Color.parseColor("#196912"));
                } else {
                    textView.setText("Expired " + Math.abs(Integer.parseInt(columnValue)) + " days ago");
                    textView.setTextColor(Color.parseColor("#b03428"));
                }
                textView.setTypeface(null, Typeface.BOLD);
            }

            textView.setPadding(10, 10, 10, 10);
            if (contractColumn.getKey().equals("contract_id")) {
                contractRow.addView(btn);
            } else {
                contractRow.addView(textView);
            }
        }

        table.addView(contractRow);
    }
}
