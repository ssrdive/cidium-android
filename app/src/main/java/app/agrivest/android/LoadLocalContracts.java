package app.agrivest.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.LinkedHashMap;

public class LoadLocalContracts implements Runnable {
    Thread thrd;
    Context context;
    SQLiteDatabase db;
    TableLayout table;
    ProgressDialog progressDialog;
    RowAdder rowAdder;

    LoadLocalContracts(Context context, SQLiteDatabase db, TableLayout table, ProgressDialog progressDialog) {
        this.context = context;
        this.db = db;
        this.table = table;
        this.progressDialog = progressDialog;
        rowAdder = new RowAdder(this.context);
        thrd = new Thread(this);
        thrd.start();
    }

    @Override
    public void run() {
        Handler mainHandler = new Handler(context.getMainLooper());
        Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.CONTRACT_TABLE_NAME, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            final TableRow contractRow = new TableRow(context);
            contractRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
            contractRow.setPadding(5, 5, 5, 5);

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
        progressDialog.dismiss();
    }
}
