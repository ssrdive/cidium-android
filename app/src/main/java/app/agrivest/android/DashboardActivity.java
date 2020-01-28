package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
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

    private void loadContracts() {
        loadContractsDialog = new ProgressDialog(this);

        contracts_TL = findViewById(R.id.contracts_TL);
        final SQLiteDatabase db = new SQLiteHelper(getApplicationContext()).getReadableDatabase();

        while (contracts_TL.getChildCount() > 1)
            contracts_TL.removeView(contracts_TL.getChildAt(contracts_TL.getChildCount() - 1));

        final RowAdder rowAdder = new RowAdder(this);
        if (connected) {
            final Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.RECEIPT_TABLE_NAME, null);
            boolean valid = res.moveToFirst();
            if (valid)
                uploadReceipt(db, res);

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
                    SetResponseContractsThread setResponseContractsThread = new SetResponseContractsThread(getApplicationContext(), db, response, contracts_TL, loadContractsDialog);
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
            loadContractsDialog.show();
            LoadLocalContractsThread loadLocalContractsThread = new LoadLocalContractsThread(this, db, contracts_TL, loadContractsDialog);
        }
    }

    public void uploadReceipt(final SQLiteDatabase db, final Cursor res) {
        String url = "https://agrivest.app/api/contract/receipt";
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
}
