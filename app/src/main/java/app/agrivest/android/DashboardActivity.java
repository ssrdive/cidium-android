package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {
    private Utils utils;
    private boolean connected;
    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    private Button refresh_contracts_BT;

    private TextView network_status_message;
    private TextView last_updated_message;

    private TableLayout contracts_TL;

    private ProgressDialog loadContractsDialog;
    private ProgressDialog uploadOfflineReceiptsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        utils = new Utils();
        connected = utils.isInternetAvailable(getApplicationContext());
        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);

        refresh_contracts_BT = findViewById(R.id.refresh_contracts_BT);
        refresh_contracts_BT.setOnClickListener(this);

        setNetworkStatusMessage();
        lastUpdated();
        loadContracts();
        uploadOfflineReceipts();
    }

    private void setNetworkStatusMessage() {
        network_status_message = findViewById(R.id.network_status_message);
        if (connected) {
            network_status_message.setTextColor(Color.parseColor("#21634b"));
            network_status_message.setText("Connected!");
        } else {
            network_status_message.setTextColor(Color.parseColor("#948b23"));
            network_status_message.setText("Disconnected! You are seeing data that was cached when you were last connected");
        }
    }

    private void lastUpdated() {
        last_updated_message = findViewById(R.id.last_updated_message);
        last_updated_message.setText("Last updated " + userDetails.getString("last_updated", ""));
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
        uploadOfflineReceipts();
    }

    private void signOut() {
        SharedPreferences.Editor userEditor = userDetails.edit();
        userEditor.clear();
        userEditor.commit();

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
    }

    private void uploadOfflineReceipts() {
        final SQLiteDatabase db = new SQLiteHelper(getApplicationContext()).getReadableDatabase();
        if (connected) {
            final Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.RECEIPT_TABLE_NAME, null);
            boolean valid = res.moveToFirst();
            if (valid)
                uploadReceipt(db, res);
        }
    }

    private void loadContracts() {
        loadContractsDialog = new ProgressDialog(this);

        contracts_TL = findViewById(R.id.contracts_TL);
        final SQLiteDatabase db = new SQLiteHelper(getApplicationContext()).getReadableDatabase();

        while (contracts_TL.getChildCount() > 1)
            contracts_TL.removeView(contracts_TL.getChildAt(contracts_TL.getChildCount() - 1));

        connected = utils.isInternetAvailable(getApplicationContext());
        if (connected) {
            lastUpdated();
            loadContractsDialog.setTitle("Loading Contracts From Server");
            loadContractsDialog.setMessage("Please wait while contracts are loaded");
            loadContractsDialog.setCancelable(false);
            if (!isFinishing()) {
                loadContractsDialog.show();
            }
            String url = new API().getApiLink() + "/contract/search?search=&state=&officer=" + userDetails.getString("id", "") + "&batch=";
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    SetResponseContractsThread setResponseContractsThread = new SetResponseContractsThread(getApplicationContext(), db, response, contracts_TL, loadContractsDialog);
                    SharedPreferences.Editor userEditor = userDetails.edit();
                    userEditor.putString("last_updated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    userEditor.commit();
                    lastUpdated();
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
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(request);
        } else {
            loadContractsDialog.setTitle("Loading Contracts From Cache");
            loadContractsDialog.setMessage("Please wait while contracts are loaded");
            loadContractsDialog.setCancelable(false);
            loadContractsDialog.show();
            LoadLocalContractsThread loadLocalContractsThread = new LoadLocalContractsThread(this, db, contracts_TL, loadContractsDialog);
        }
    }

    public void uploadReceipt(final SQLiteDatabase db, final Cursor res) {
        String url = new API().getApiLink() + "/contract/receipt";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                db.delete(SQLiteHelper.RECEIPT_TABLE_NAME, "id = ?", new String[]{res.getString(res.getColumnIndex("id"))});
                res.moveToNext();
                if(res.isAfterLast() == false)
                    uploadReceipt(db, res);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null) {
                    switch (networkResponse.statusCode) {
                        case 500:
                            Toast.makeText(getApplicationContext(), "Error uploading offline receipts. Please check the amounts and validate with the office", Toast.LENGTH_LONG).show();
                            break;
                        case 400:
                            signOut();
                            break;
                    }
                    Log.d("STATUS_CODE", String.valueOf(networkResponse.statusCode));
                }
                res.moveToNext();
                if(res.isAfterLast() == false)
                    uploadReceipt(db, res);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cid", res.getString(res.getColumnIndex("contract_id")));
                params.put("user_id", res.getString(res.getColumnIndex("user_id")));
                params.put("amount", res.getString(res.getColumnIndex("amount")));
                if(res.getString(res.getColumnIndex("payment_type")).equals("Cash")) {
                    params.put("due_date", "");
                } else {
                    params.put("due_date", res.getString(res.getColumnIndex("due_date")));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + userDetails.getString("token", ""));
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.offline_receipts:
                intent = new Intent(getApplicationContext(), OfflineReceiptsActivity.class);
                startActivity(intent);
                break;
            case R.id.issued_receipts:
                intent = new Intent(getApplicationContext(), IssuedReceiptsActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out:
                signOut();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_contracts_BT:
                connected = utils.isInternetAvailable(getApplicationContext());
                if(!connected) {
                    Toast.makeText(getApplicationContext(), "You are offline", Toast.LENGTH_SHORT).show();
                } else {
                    loadContracts();
                }
                break;
        }
    }
}
