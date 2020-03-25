package app.agrivest.android.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import app.agrivest.android.R;
import app.agrivest.android.utils.RowAdder;
import app.agrivest.android.utils.Utils;
import app.agrivest.android.api.API;
import app.agrivest.android.threads.PrintReceiptSummaryThread;

public class IssuedReceiptsActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;

    Utils utils;

    private ProgressDialog loadReceipts;

    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    JSONArray receipts = null;

    TableLayout receiptsTable;
    Button print_summary_BT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issued_receipts);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Issued Receipts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        utils = new Utils();
        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);
        receiptsTable = findViewById(R.id.receipts_TL);
        print_summary_BT = findViewById(R.id.print_summary_BT);
        print_summary_BT.setOnClickListener(this);
        loadReceipts();
    }


    private void loadReceipts() {
        if (utils.isInternetAvailable(this)) {
            loadReceipts = new ProgressDialog(this);
            loadReceipts.setTitle("Loading receipts");
            loadReceipts.setMessage("Please wait while receipts are loaded");
            loadReceipts.setCancelable(false);
            loadReceipts.show();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String url = new API().getApiLink() + "/contract/receipts/officer/" + userDetails.getString("id", "") + "/" + formatter.format(new Date());
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        receipts = new JSONArray(response);
                        RowAdder rowAdder = new RowAdder(getApplicationContext());
                        for (int i = 0; i < receipts.length(); i++) {
                            JSONObject receipt = receipts.getJSONObject(i);
                            Iterator<String> installmentIterator = receipt.keys();
                            String tableExp[] = {};
                            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                            while (installmentIterator.hasNext()) {
                                String key = installmentIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    if (key.equals("notes")) {
                                        contractMap.put(key, new JSONObject(receipt.getString(key)).getString("String"));
                                    } else {
                                        contractMap.put(key, receipt.getString(key));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.print_summary_BT:
                loadReceipts = new ProgressDialog(this);
                loadReceipts.setTitle("Printing receipt summary");
                loadReceipts.setMessage("Please wait while receipt summary is printed");
                loadReceipts.setCancelable(false);
                loadReceipts.show();
                PrintReceiptSummaryThread printReceiptSummaryThread = new PrintReceiptSummaryThread(this, loadReceipts, receipts);
                break;
        }
    }
}
