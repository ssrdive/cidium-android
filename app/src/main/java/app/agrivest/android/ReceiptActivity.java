package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;

public class ReceiptActivity extends AppCompatActivity implements View.OnClickListener {
    private String id;
    private String customer_name;
    private String customer_contact;
    private String chassis_number;
    private float amount_pending;
    private float total_payable;

    private TextView id_TV;
    private TextView customer_name_TV;
    private TextView chassis_number_TV;
    private TextView amount_pending_TV;
    private TextView total_payable_TV;

    private EditText amount_ET;

    private Button issue_receipt_BT;

    private ProgressDialog loadContractsDialog;
    private AlertDialog printReceiptStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        customer_name = bundle.getString("customer_name");
        customer_contact = bundle.getString("customer_contact");
        chassis_number = bundle.getString("chassis_number");
        amount_pending = Float.parseFloat(bundle.getString("amount_pending"));
        total_payable = Float.parseFloat(bundle.getString("total_payable"));

        getSupportActionBar().setTitle("Issue Receipt " + id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id_TV = findViewById(R.id.id_TV);
        customer_name_TV = findViewById(R.id.customer_name_TV);
        chassis_number_TV = findViewById(R.id.chassis_number_TV);
        amount_pending_TV = findViewById(R.id.amount_pending_TV);
        total_payable_TV = findViewById(R.id.total_payable_TV);

        NumberFormatter formatter = new NumberFormatter();

        id_TV.setText(id);
        customer_name_TV.setText(customer_name);
        chassis_number_TV.setText(chassis_number);
        amount_pending_TV.setText(formatter.format(amount_pending));
        if (amount_pending > 0) {
            amount_pending_TV.setTextColor(Color.parseColor("#b03428"));
        } else {
            amount_pending_TV.setTextColor(Color.parseColor("#196912"));
        }
        amount_pending_TV.setTypeface(null, Typeface.BOLD);
        total_payable_TV.setText(formatter.format(total_payable));

        issue_receipt_BT = findViewById(R.id.issue_receipt_BT);
        issue_receipt_BT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.issue_receipt_BT:
                amount_ET = findViewById(R.id.amount_ET);
                final String amount = amount_ET.getText().toString();
                if (amount.equals("") || Float.parseFloat(amount) == 0) {
                    printReceiptStatus = new AlertDialog.Builder(this).create();
                    printReceiptStatus.setTitle("Invalid receipt amount");
                    printReceiptStatus.setMessage("Please enter a valid receipt amount");
                    printReceiptStatus.setIcon(R.drawable.failure_icon);
                    printReceiptStatus.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    printReceiptStatus.show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(new NumberFormatter().format(amount_ET.getText().toString()));
                builder.setMessage("Please confirm whether the amount is correct");
                builder.setIcon(R.drawable.confirmation_icon);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id2) {
                        dialog.dismiss();
                        loadContractsDialog = new ProgressDialog(ReceiptActivity.this);
                        printReceiptStatus = new AlertDialog.Builder(ReceiptActivity.this).create();
                        loadContractsDialog.setTitle("Issuing receipt");
                        loadContractsDialog.setMessage("Please wait while the receipt is issued");
                        loadContractsDialog.setCancelable(false);
                        loadContractsDialog.show();
                        HashMap<String, String> receiptDetails = new HashMap<String, String>();
                        receiptDetails.put("id", id);
                        receiptDetails.put("customer_name", customer_name);
                        receiptDetails.put("customer_contact", customer_contact);
                        receiptDetails.put("chassis_number", chassis_number);
                        receiptDetails.put("amount_pending", String.valueOf(amount_pending));
                        receiptDetails.put("total_payable", String.valueOf(total_payable));
                        receiptDetails.put("amount", amount);
                        IssueReceiptThread issueReceiptThread = new IssueReceiptThread(ReceiptActivity.this, loadContractsDialog, receiptDetails);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
    }
}
