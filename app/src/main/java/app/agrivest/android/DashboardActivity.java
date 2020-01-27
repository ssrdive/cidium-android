package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private Utils utils;
    private boolean connected;
    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    private TextView network_status_message;

    private TableLayout contracts_TL;

    private ProgressDialog loadContractsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        utils = new Utils();
        connected = utils.isInternetAvailable(getApplicationContext());
        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);

        setNetworkStatusMessage();
        loadContracts();
    }

    private void setNetworkStatusMessage() {
        network_status_message = findViewById(R.id.network_status_message);
        if (connected) {
            network_status_message.setTextColor(Color.parseColor("#21634b"));
            network_status_message.setText("Connected! You are seeing real-time data");
        } else {
            network_status_message.setTextColor(Color.parseColor("#948b23"));
            network_status_message.setText("Disconnected! You are seeing data that was cached when you were last connected");
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        connected = utils.isInternetAvailable(getApplicationContext());

        setNetworkStatusMessage();
        loadContracts();
    }

    private void signOut() {
        SharedPreferences.Editor userEditor = userDetails.edit();
        userEditor.clear();
        userEditor.commit();

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
    }

    private void addContractRowToTable(TableLayout table, HashMap<String, String> contract) {
        TableRow contractRow = new TableRow(getApplicationContext());
        contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
        contractRow.setPadding(5, 5, 5, 5);

        Iterator contractIterator = contract.entrySet().iterator();
        while (contractIterator.hasNext()) {
            Map.Entry contractColumn = (Map.Entry) contractIterator.next();
            Button btn = new Button(getApplicationContext());
            TextView textView = new TextView(getApplicationContext());
            String columnValue = contractColumn.getValue().toString();

            if (contractColumn.getKey().equals("id") || contractColumn.getKey().equals("total_payable")) {
                final String id = contract.get("id");
                final String customer_name = contract.get("customer_name");
                final String chassis_number = contract.get("chassis_number");
                final String amount_pending = contract.get("amount_pending");
                final String total_payable = contract.get("total_payable");

                btn.setClickable(true);
                btn.setText(contractColumn.getValue().toString());
                final String columnKey = contractColumn.getKey().toString();
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = (columnKey.equals("id")) ? new Intent(DashboardActivity.this, ContractDetailsActivity.class) : new Intent(DashboardActivity.this, ReceiptActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("customer_name", customer_name);
                        intent.putExtra("chassis_number", chassis_number);
                        intent.putExtra("amount_pending", amount_pending);
                        intent.putExtra("total_payable", total_payable);
                        startActivity(intent);
                    }
                });
            }

            if (!contractColumn.getKey().equals("0") && (contractColumn.getKey().equals("amount_pending") || contractColumn.getKey().equals("total_payable") || contractColumn.getKey().equals("total_payable") || contractColumn.getKey().equals("total_paid"))) {
                double amount = Double.parseDouble(contractColumn.getValue().toString());
                DecimalFormat formatter = new DecimalFormat("#,###.00");

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

            if (contractColumn.getKey().equals("customer_contact")) {
                textView.setClickable(true);
                textView.setTextColor(Color.parseColor("#3269a8"));
                final String customer_contact = contract.get("customer_contact");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel://" + customer_contact)));
                    }
                });
            }

            textView.setText(columnValue);
            textView.setPadding(10, 10, 10, 10);
            if (contractColumn.getKey().equals("id") || contractColumn.getKey().equals("total_payable")) {
                contractRow.addView(btn);
            } else {
                contractRow.addView(textView);
            }
        }

        table.addView(contractRow);
    }

    private void loadContracts() {
        loadContractsDialog = new ProgressDialog(this);

        contracts_TL = findViewById(R.id.contracts_TL);
        final SQLiteDatabase db = new SQLiteHelper(getApplicationContext()).getReadableDatabase();

        while (contracts_TL.getChildCount() > 1)
            contracts_TL.removeView(contracts_TL.getChildAt(contracts_TL.getChildCount() - 1));

        if (connected) {
            loadContractsDialog.setTitle("Loading Contracts From Server");
            loadContractsDialog.setMessage("Please wait while contracts are loaded");
            loadContractsDialog.setCancelable(false);
            if (!isFinishing()) {
                loadContractsDialog.show();
            }
            String url = "https://agrivest.app/api/contract/search?search=&state=&officer=" + userDetails.getString("id", "") + "&batch=";
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray contracts = new JSONArray(response);
                        db.execSQL("DELETE FROM " + SQLiteHelper.CONTRACT_TABLE_NAME);

                        for (int i = 0; i < contracts.length(); i++) {
                            JSONObject contract = contracts.getJSONObject(i);
                            Iterator<String> contractIterator = contract.keys();
                            String tableExp[] = {"recovery_officer", "total_di_paid"};
                            HashMap<String, String> contractMap = new HashMap<String, String>();
                            ContentValues contractValues = new ContentValues();
                            while (contractIterator.hasNext()) {
                                String key = contractIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    contractMap.put(key, contract.getString(key));
                                    contractValues.put(key, contract.getString(key));
                                }
                            }
                            addContractRowToTable(contracts_TL, contractMap);
                            db.insert(SQLiteHelper.CONTRACT_TABLE_NAME, null, contractValues);
                            loadContractsDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        loadContractsDialog.dismiss();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadContractsDialog.dismiss();
                    error.printStackTrace();
                    signOut();
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
        } else {
            loadContractsDialog.setTitle("Loading Contracts From Cache");
            loadContractsDialog.setMessage("Please wait while contracts are loaded");
            loadContractsDialog.setCancelable(false);
            if (!isFinishing()) {
                loadContractsDialog.show();
            }
            Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.CONTRACT_TABLE_NAME, null);
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                final TableRow contractRow = new TableRow(getApplicationContext());
                contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
                contractRow.setPadding(5, 5, 5, 5);

                for (final String column : res.getColumnNames()) {
                    Button btn = new Button(getApplicationContext());
                    TextView textView = new TextView(getApplicationContext());
                    String columnValue = res.getString(res.getColumnIndex(column));

                    if (column.equals("id") || column.equals("total_payable")) {
                        final String id = res.getString(res.getColumnIndex("id"));
                        final String customer_name = res.getString(res.getColumnIndex("customer_name"));
                        final String chassis_number = res.getString(res.getColumnIndex("chassis_number"));
                        final String amount_pending = res.getString(res.getColumnIndex("amount_pending"));
                        final String total_payable = res.getString(res.getColumnIndex("total_payable"));

                        btn.setClickable(true);
                        btn.setText(columnValue);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = (column.equals("id")) ? new Intent(DashboardActivity.this, ContractDetailsActivity.class) : new Intent(DashboardActivity.this, ReceiptActivity.class);
                                intent.putExtra("id", id);
                                intent.putExtra("customer_name", customer_name);
                                intent.putExtra("chassis_number", chassis_number);
                                intent.putExtra("amount_pending", amount_pending);
                                intent.putExtra("total_payable", total_payable);
                                startActivity(intent);
                            }
                        });
                    }

                    if (!res.getString(res.getColumnIndex(column)).equals("0") && (column.equals("amount_pending") || column.equals("total_payable") || column.equals("total_payable") || column.equals("total_paid"))) {
                        double amount = Double.parseDouble(res.getString(res.getColumnIndex(column)));
                        DecimalFormat formatter = new DecimalFormat("#,###.00");

                        columnValue = formatter.format(amount);
                    }

                    if (column.equals("amount_pending")) {
                        textView.setTypeface(null, Typeface.BOLD);
                        if (Float.parseFloat(res.getString(res.getColumnIndex(column))) > 0) {
                            textView.setTextColor(Color.parseColor("#b03428"));
                        } else {
                            textView.setTextColor(Color.parseColor("#196912"));
                        }
                    }

                    if (column.equals("customer_contact")) {
                        textView.setClickable(true);
                        textView.setTextColor(Color.parseColor("#3269a8"));
                        final String customer_contact = res.getString(res.getColumnIndex("customer_contact"));
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel://" + customer_contact)));
                            }
                        });
                    }

                    textView.setText(columnValue);
                    textView.setPadding(10, 10, 10, 10);
                    if (column.equals("id") || column.equals("total_payable")) {
                        contractRow.addView(btn);
                    } else {
                        contractRow.addView(textView);
                    }
                }

                contracts_TL.addView(contractRow);

                res.moveToNext();
            }
            loadContractsDialog.dismiss();
        }
    }
}
