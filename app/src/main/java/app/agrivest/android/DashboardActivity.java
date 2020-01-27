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
import java.util.LinkedHashMap;
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
                    SetResponseContracts setResponseContracts = new SetResponseContracts(getApplicationContext(), db, response, contracts_TL, loadContractsDialog);
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
            LoadLocalContracts loadLocalContracts = new LoadLocalContracts(this, db, contracts_TL, loadContractsDialog);
        }
    }
}
