package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
        chassis_number = bundle.getString("chassis_number");
        amount_pending = Float.parseFloat(bundle.getString("amount_pending"));
        total_payable = Float.parseFloat(bundle.getString("total_payable"));

        id_TV = findViewById(R.id.id_TV);
        customer_name_TV = findViewById(R.id.customer_name_TV);
        chassis_number_TV = findViewById(R.id.chassis_number_TV);
        amount_pending_TV = findViewById(R.id.amount_pending_TV);
        total_payable_TV = findViewById(R.id.total_payable_TV);

        DecimalFormat formatter = new DecimalFormat("#,###.00");

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
                loadContractsDialog = new ProgressDialog(ReceiptActivity.this);
                printReceiptStatus = new AlertDialog.Builder(ReceiptActivity.this).create();
                amount_ET = findViewById(R.id.amount_ET);
                loadContractsDialog.setTitle("Issuing receipt");
                loadContractsDialog.setMessage("Please wait while the receipt is issued");
                loadContractsDialog.setCancelable(false);
                loadContractsDialog.show();
                HashMap<String, String> receiptDetails = new HashMap<String, String>();
                receiptDetails.put("id", id);
                receiptDetails.put("customer_name", customer_name);
                receiptDetails.put("chassis_number", chassis_number);
                receiptDetails.put("amount", amount_ET.getText().toString());
                IssueReceipt issueReceipt = new IssueReceipt(this, loadContractsDialog, receiptDetails);
                break;
        }
    }
}
