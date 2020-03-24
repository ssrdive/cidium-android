package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContractDetailsActivity extends AppCompatActivity {
    TextView id_TV;
    TextView customer_name_TV;
    TextView customer_address_TV;
    TextView model_TV;
    TextView amount_pending_TV;
    Button total_payable_TV;
    TextView chassis_number_TV;
    TextView contact_TV;

    Utils utils;

    private ProgressDialog loadReceipts;
    private ProgressDialog loadInstallments;

    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    TableLayout receiptsTable;
    TableLayout installmentsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_details);

        Bundle bundle = getIntent().getExtras();
        final String id = bundle.getString("id");
        final String agrivest = bundle.getString("agrivest");
        final String customer_name = bundle.getString("customer_name");
        final String customer_address = bundle.getString("customer_address");
        final String model = bundle.getString("model");
        final String customer_contact = bundle.getString("customer_contact");
        final String chassis_number = bundle.getString("chassis_number");
        final String amount_pending = bundle.getString("amount_pending");
        final String total_payable = bundle.getString("total_payable");

        id_TV = findViewById(R.id.id_TV);
        customer_name_TV = findViewById(R.id.customer_name_TV);
        customer_address_TV = findViewById(R.id.customer_address_TV);
        model_TV = findViewById(R.id.model_TV);
        contact_TV = findViewById(R.id.contact_TV);
        chassis_number_TV = findViewById(R.id.chassis_number_TV);
        amount_pending_TV = findViewById(R.id.amount_pending_TV);
        total_payable_TV = findViewById(R.id.total_payable_TV);

        total_payable_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReceiptActivity.class);
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
                getApplicationContext().startActivity(intent);
            }
        });

        id_TV.setText(id);
        customer_name_TV.setText(customer_name);
        customer_address_TV.setText(customer_address);
        model_TV.setText(model);
        chassis_number_TV.setText(chassis_number);

        NumberFormatter formatter = new NumberFormatter();

        amount_pending_TV.setText(formatter.format(amount_pending));
        total_payable_TV.setText(formatter.format(total_payable));

        contact_TV.setText(customer_contact);
        contact_TV.setClickable(true);
        contact_TV.setTextColor(Color.parseColor("#3269a8"));
        contact_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel://" + customer_contact)));
            }
        });

        getSupportActionBar().setTitle("Contract Details " + id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        utils = new Utils();
        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);
        receiptsTable = findViewById(R.id.receipts_TL);
        installmentsTable = findViewById(R.id.installments_TL);
        loadReceipts(id);
        loadInstallments(id);
    }

    private void loadReceipts(String id) {
        if(utils.isInternetAvailable(this)) {
            loadReceipts = new ProgressDialog(this);
            loadReceipts.setTitle("Loading receipts");
            loadReceipts.setMessage("Please wait while receipts are loaded");
            loadReceipts.setCancelable(false);
            String url =  new API().getApiLink() + "/contract/receipts/" + id;
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray installments = new JSONArray(response);
                        RowAdder rowAdder = new RowAdder(getApplicationContext());
                        for (int i = 0; i < installments.length(); i++) {
                            JSONObject installment = installments.getJSONObject(i);
                            Iterator<String> installmentIterator = installment.keys();
                            String tableExp[] = {};
                            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                            while (installmentIterator.hasNext()) {
                                String key = installmentIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    if(key.equals("notes")) {
                                        contractMap.put(key, new JSONObject(installment.getString(key)).getString("String"));
                                    } else {
                                        contractMap.put(key, installment.getString(key));
                                    }
                                }
                            }
                            rowAdder.receipt(receiptsTable, contractMap);
                        }
                        loadReceipts.dismiss();
                    } catch (JSONException e) {
                        loadReceipts.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadReceipts.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + userDetails.getString("token", ""));
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };

            mQueue.add(request);
        }
    }

    private void loadInstallments(String id) {
        if(utils.isInternetAvailable(this)) {
            loadInstallments = new ProgressDialog(this);
            loadInstallments.setTitle("Loading Installments");
            loadInstallments.setMessage("Please wait while installments are loaded");
            loadInstallments.setCancelable(false);
            String url = new API().getApiLink() + "/contract/installments/" + id;
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray installments = new JSONArray(response);
                        RowAdder rowAdder = new RowAdder(getApplicationContext());
                        for (int i = 0; i < installments.length(); i++) {
                            JSONObject installment = installments.getJSONObject(i);
                            Iterator<String> installmentIterator = installment.keys();
                            String tableExp[] = {"id"};
                            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                            while (installmentIterator.hasNext()) {
                                String key = installmentIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    contractMap.put(key, installment.getString(key));
                                }
                            }
                            rowAdder.installment(installmentsTable, contractMap);
                        }
                        loadInstallments.dismiss();
                    } catch (JSONException e) {
                        loadInstallments.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadInstallments.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + userDetails.getString("token", ""));
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };

            mQueue.add(request);
        }
    }
}
