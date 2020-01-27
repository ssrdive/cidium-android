package app.agrivest.android;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.TableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class SetResponseContracts implements Runnable {
    Thread thrd;
    Context context;
    SQLiteDatabase db;
    String response;
    TableLayout table;
    ProgressDialog progressDialog;
    RowAdder rowAdder;

    SetResponseContracts(Context context, SQLiteDatabase db, String response, TableLayout table, ProgressDialog progressDialog) {
        this.context = context;
        this.db = db;
        this.response = response;
        this.table = table;
        this.progressDialog = progressDialog;
        rowAdder = new RowAdder(this.context);
        thrd = new Thread(this);
        thrd.start();
    }

    @Override
    public void run() {
        Handler mainHandler = new Handler(context.getMainLooper());
        try {
            JSONArray contracts = new JSONArray(response);
            db.execSQL("DELETE FROM " + SQLiteHelper.CONTRACT_TABLE_NAME);

            for (int i = 0; i < contracts.length(); i++) {
                JSONObject contract = contracts.getJSONObject(i);
                Iterator<String> contractIterator = contract.keys();
                String tableExp[] = {"recovery_officer", "total_di_paid"};
                final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                ContentValues contractValues = new ContentValues();
                while (contractIterator.hasNext()) {
                    String key = contractIterator.next();
                    if (!Arrays.asList(tableExp).contains(key)) {
                        contractMap.put(key, contract.getString(key));
                        contractValues.put(key, contract.getString(key));
                    }
                }
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        rowAdder.contract(table, contractMap);
                    }
                };
                mainHandler.post(myRunnable);
                db.insert(SQLiteHelper.CONTRACT_TABLE_NAME, null, contractValues);
                progressDialog.dismiss();
            }

        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }
}
