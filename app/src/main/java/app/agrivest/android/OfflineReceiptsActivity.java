package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedHashMap;

public class OfflineReceiptsActivity extends AppCompatActivity {

    TableLayout offline_receipts_TL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_receipts);

        getSupportActionBar().setTitle("Offline Receipts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        offline_receipts_TL = findViewById(R.id.offline_receipts_TL);

        SQLiteDatabase db = new SQLiteHelper(this).getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.RECEIPT_TABLE_NAME, null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            TableRow receiptRow = new TableRow(this);
            receiptRow.setBackgroundColor(Color.parseColor("#d5f0dc"));
            receiptRow.setPadding(5, 5, 5, 5);
            for (final String column : res.getColumnNames()) {
                TextView textView = new TextView(this);
                textView.setText(res.getString(res.getColumnIndex(column)));
                if (!(column.equals("id") || column.equals("user_id"))) {
                    textView.setPadding(10, 10, 10, 10);
                    receiptRow.addView(textView);
                }
            }
            offline_receipts_TL.addView(receiptRow);
            res.moveToNext();
        }
        db.close();
    }
}
