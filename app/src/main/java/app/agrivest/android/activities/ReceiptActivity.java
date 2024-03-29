package app.agrivest.android.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import app.agrivest.android.utils.NumberFormatter;
import app.agrivest.android.R;
import app.agrivest.android.utils.SQLiteHelper;
import app.agrivest.android.utils.Utils;
import app.agrivest.android.threads.IssueReceiptThread;

public class ReceiptActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;

    private String id;
    private String agrivest;
    private String customer_name;
    private String customer_address;
    private String customer_contact;
    private String chassis_number;
    private float amount_pending;
    private float total_payable;
    private float default_charges;

    private TextView id_TV;
    private TextView customer_name_TV;
    private TextView customer_address_TV;
    private TextView chassis_number_TV;
    private TextView amount_pending_TV;
    private TextView total_payable_TV;
    private TextView default_charges_TV;

    private EditText amount_ET;
    private EditText due_date_ET;

    private Button issue_receipt_BT;

    private Spinner payment_method_SP;

    private TextView due_date_TV;

    private Utils utils;

    private ProgressDialog loadContractsDialog;
    private AlertDialog printReceiptStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");

        SQLiteDatabase db = new SQLiteHelper(this).getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.CONTRACT_TABLE_NAME + " WHERE id = " + id, null);
        res.moveToFirst();

        agrivest = res.getString(res.getColumnIndex("agrivest"));
        customer_name = res.getString(res.getColumnIndex("customer_name"));
        customer_address = res.getString(res.getColumnIndex("customer_address"));
        customer_contact = res.getString(res.getColumnIndex("customer_contact"));
        chassis_number = res.getString(res.getColumnIndex("chassis_number"));
        amount_pending = Float.parseFloat(res.getString(res.getColumnIndex("amount_pending")));
        total_payable = Float.parseFloat(res.getString(res.getColumnIndex("total_payable")));
        default_charges = Float.parseFloat(res.getString(res.getColumnIndex("default_charges")));

        db.close();

        getSupportActionBar().setTitle("Issue Receipt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        utils = new Utils();

        id_TV = findViewById(R.id.id_TV);
        customer_name_TV = findViewById(R.id.customer_name_TV);
        customer_address_TV = findViewById(R.id.customer_address_TV);
        chassis_number_TV = findViewById(R.id.chassis_number_TV);
        amount_pending_TV = findViewById(R.id.amount_pending_TV);
        total_payable_TV = findViewById(R.id.total_payable_TV);
        default_charges_TV = findViewById(R.id.default_charges_TV);
        due_date_ET = findViewById(R.id.due_date_ET);
        due_date_ET.setEnabled(false);
        due_date_ET.setInputType(InputType.TYPE_NULL);
        due_date_ET.setFocusable(false);
        payment_method_SP = findViewById(R.id.payment_method_SP);
        due_date_TV = findViewById(R.id.due_date_TV);

        payment_method_SP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals("Check")) {
                    due_date_ET.setEnabled(true);
                    due_date_ET.setInputType(InputType.TYPE_CLASS_TEXT);
                    due_date_ET.setFocusable(true);
                } else {
                    due_date_ET.setEnabled(false);
                    due_date_ET.setInputType(InputType.TYPE_NULL);
                    due_date_ET.setFocusable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        NumberFormatter formatter = new NumberFormatter();

        id_TV.setText(id);
        customer_name_TV.setText(customer_name);
        customer_address_TV.setText(customer_address);
        chassis_number_TV.setText(chassis_number);
        amount_pending_TV.setText(formatter.format(amount_pending));
        if (amount_pending > 0) {
            amount_pending_TV.setTextColor(Color.parseColor("#b03428"));
        } else {
            amount_pending_TV.setTextColor(Color.parseColor("#196912"));
        }
        amount_pending_TV.setTypeface(null, Typeface.BOLD);
        total_payable_TV.setText(formatter.format(total_payable));
        default_charges_TV.setText(formatter.format(default_charges));
        if (default_charges > 0) {
            default_charges_TV.setTextColor(Color.parseColor("#b03428"));
        } else {
            default_charges_TV.setTextColor(Color.parseColor("#196912"));
        }

        issue_receipt_BT = findViewById(R.id.issue_receipt_BT);
        issue_receipt_BT.setOnClickListener(this);
        due_date_ET.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.due_date_ET:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        due_date_ET.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                }, utils.getYear(), utils.getMonth() - 1, utils.getDay());
                datePickerDialog.show();
                break;
            case R.id.issue_receipt_BT:
                amount_ET = findViewById(R.id.amount_ET);
                final String amount = amount_ET.getText().toString();
                final String due_date = due_date_ET.getText().toString();
                final String paymentType = payment_method_SP.getSelectedItem().toString();
                if(paymentType.equals("Check") && due_date.equals("")) {
                    Toast.makeText(this, "Enter due date", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
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
                        receiptDetails.put("agrivest", agrivest);
                        receiptDetails.put("customer_name", customer_name);
                        receiptDetails.put("customer_contact", customer_contact);
                        receiptDetails.put("chassis_number", chassis_number);
                        receiptDetails.put("amount_pending", String.valueOf(amount_pending));
                        receiptDetails.put("total_payable", String.valueOf(total_payable));
                        receiptDetails.put("amount", amount);
                        receiptDetails.put("due_date", due_date);
                        receiptDetails.put("payment_type", paymentType);
                        receiptDetails.put("checksum", generateChecksum(id));
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

    private String generateChecksum(String id) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return formatter.format(date)+ id;
    }
}
