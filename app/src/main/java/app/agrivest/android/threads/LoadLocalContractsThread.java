package app.agrivest.android.threads;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.widget.TableLayout;

import java.util.LinkedHashMap;

import app.agrivest.android.utils.RowAdder;
import app.agrivest.android.utils.SQLiteHelper;

public class LoadLocalContractsThread implements Runnable {
    Thread thrd;
    Context context;
    SQLiteDatabase db;
    TableLayout table;
    ProgressDialog progressDialog;
    String keyword;
    RowAdder rowAdder;

    public LoadLocalContractsThread(Context context, SQLiteDatabase db, TableLayout table, ProgressDialog progressDialog, String keyword) {
        this.context = context;
        this.db = db;
        this.table = table;
        this.progressDialog = progressDialog;
        this.keyword = keyword;
        rowAdder = new RowAdder(this.context);
        thrd = new Thread(this);
        thrd.start();
    }

    @Override
    public void run() {
        Handler mainHandler = new Handler(context.getMainLooper());
        Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.CONTRACT_TABLE_NAME + " WHERE id || batch || chassis_number || customer_name || customer_address LIKE \'%" + keyword + "%\';", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
            for (final String column : res.getColumnNames()) {
                contractMap.put(column, res.getString(res.getColumnIndex(column)));
            }
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    rowAdder.contract(table, contractMap);
                }
            };
            mainHandler.post(myRunnable);
            res.moveToNext();
        }
        db.close();
        progressDialog.dismiss();
    }
}
