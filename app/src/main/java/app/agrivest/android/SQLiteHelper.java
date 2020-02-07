package app.agrivest.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "agrivest.db";

    public static final String CONTRACT_TABLE_NAME = "contract";
    public static final String CONTRACT_COLUMN_ID = "id";
    public static final String CONTRACT_COLUMN_AGRIVEST = "agrivest";
    public static final String CONTRACT_COLUMN_STATE = "state";
    public static final String CONTRACT_COLUMN_MODEL = "model";
    public static final String CONTRACT_COLUMN_CHASSIS_NUMBER = "chassis_number";
    public static final String CONTRACT_COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String CONTRACT_COLUMN_CUSTOMER_CONTACT = "customer_contact";
    public static final String CONTRACT_COLUMN_AMOUNT_PENDING = "amount_pending";
    public static final String CONTRACT_COLUMN_TOTAL_PAYABLE = "total_payable";
    public static final String CONTRACT_COLUMN_TOTAL_AGREEMENT = "total_agreement";
    public static final String CONTRACT_COLUMN_TOTAL_PAID = "total_paid";

    public static final String RECEIPT_TABLE_NAME = "receipt";
    public static final String RECEIPT_COLUMN_ID = "id";
    public static final String RECEIPT_CONTRACT_ID = "contract_id";
    public static final String RECEIPT_CUSTOMER_NAME = "customer_name";
    public static final String RECEIPT_USER_ID = "user_id";
    public static final String RECEIPT_AMOUNT = "amount";

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CONTRACT_TABLE_NAME + "(" +
                CONTRACT_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                CONTRACT_COLUMN_AGRIVEST + " TEXT, " +
                CONTRACT_COLUMN_STATE + " TEXT, " +
                CONTRACT_COLUMN_MODEL + " TEXT, " +
                CONTRACT_COLUMN_CHASSIS_NUMBER + " TEXT, " +
                CONTRACT_COLUMN_CUSTOMER_NAME + " TEXT, " +
                CONTRACT_COLUMN_CUSTOMER_CONTACT + " TEXT, " +
                CONTRACT_COLUMN_AMOUNT_PENDING + " TEXT, " +
                CONTRACT_COLUMN_TOTAL_PAYABLE + " TEXT, " +
                CONTRACT_COLUMN_TOTAL_AGREEMENT + " TEXT, " +
                CONTRACT_COLUMN_TOTAL_PAID + " TEXT " + ")");
        db.execSQL("CREATE TABLE " + RECEIPT_TABLE_NAME + "(" +
                RECEIPT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RECEIPT_CONTRACT_ID + " INTEGER, " +
                RECEIPT_CUSTOMER_NAME + " TEXT, " +
                RECEIPT_USER_ID + " INTEGER, " +
                RECEIPT_AMOUNT + " INTEGER" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTRACT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RECEIPT_TABLE_NAME);
        onCreate(db);
    }
}
