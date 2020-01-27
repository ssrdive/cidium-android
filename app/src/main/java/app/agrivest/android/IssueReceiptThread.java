package app.agrivest.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bxl.config.editor.BXLConfigLoader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.widget.Toast;

import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

public class IssueReceiptThread implements Runnable {
    Thread thrd;
    Context context;
    ProgressDialog progressDialog;
    HashMap<String, String> receiptDetails;
    Utils utils;
    AlertDialog printReceiptStatus;
    Handler mainHandler;
    SharedPreferences userDetails;
    RequestQueue mQueue;

    IssueReceiptThread(Context context, ProgressDialog progressDialog, HashMap<String, String> receiptDetails) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.receiptDetails = receiptDetails;
        utils = new Utils();
        thrd = new Thread(this);
        thrd.start();
    }

    private void showMessage(final String type, final String title, final String message) {
        progressDialog.dismiss();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                printReceiptStatus = new AlertDialog.Builder(context).create();
                printReceiptStatus.setTitle(title);
                printReceiptStatus.setMessage(message);
                printReceiptStatus.setIcon(context.getResources().getDrawable(type.equals("success") ? R.drawable.success_icon : R.drawable.failure_icon));
                printReceiptStatus.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                printReceiptStatus.show();
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void run() {
        mainHandler = new Handler(context.getMainLooper());

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter.getBondedDevices();

        Iterator<BluetoothDevice> btItr = bondedDeviceSet.iterator();

        boolean paired = false;
        String MAC = null;
        while (btItr.hasNext()) {
            BluetoothDevice bt = btItr.next();
            if (bt.getName().equals(BXLConfigLoader.PRODUCT_NAME_SPP_R210)) {
                paired = true;
                MAC = bt.getAddress();
            }
        }

        if (!paired) {
            showMessage(
                    "Failure",
                    "Printer not paired",
                    "Your device is not paired with the printer. Please go to bluetooth settings and make sure " + BXLConfigLoader.PRODUCT_NAME_SPP_R210 + " exists under paired devices section."
            );
            return;
        }

        BXLConfigLoader bxlConfigLoader = new BXLConfigLoader(context);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }

        try {
            for (Object entry : bxlConfigLoader.getEntries()) {
                JposEntry jposEntry = (JposEntry) entry;
                bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(
                    "Failure",
                    "Something went wrong",
                    "Please contact system administrator"
            );
            return;
        }

        try {
            bxlConfigLoader.addEntry(BXLConfigLoader.PRODUCT_NAME_SPP_R210,
                    BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                    BXLConfigLoader.PRODUCT_NAME_SPP_R210,
                    BXLConfigLoader.DEVICE_BUS_BLUETOOTH,
                    MAC);
            bxlConfigLoader.saveFile();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(
                    "Failure",
                    "Something went wrong",
                    "Please contact system administrator"
            );
            return;
        }

        try {
            POSPrinter posPrinter = new POSPrinter(context);
            posPrinter.open(BXLConfigLoader.PRODUCT_NAME_SPP_R210);
            posPrinter.claim(5000);
            posPrinter.setDeviceEnabled(true);
            posPrinter.checkHealth(JposConst.JPOS_CH_INTERNAL);
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Agrivest (Private) Limited\n" +
                    "Customer Name " + receiptDetails.get("customer_name") + "\n" +
                    "Chassis Number " + receiptDetails.get("chassis_number") + "\n" +
                    "Amount LKR " + receiptDetails.get("amount") + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            posPrinter.close();
        } catch (JposException e) {
            e.printStackTrace();
            showMessage(
                    "Failure",
                    "Printing error",
                    "Please check the following details\n" +
                            "1. Printer is turned on\n" +
                            "2. Printer is within 1m range\n" +
                            "3. Paper roll not empty\n" +
                            "4. Paper cover closed"
            );
            return;
        }

        userDetails = context.getSharedPreferences("user_details", context.MODE_PRIVATE);
        if(utils.isInternetAvailable(context)) {
            mQueue = Volley.newRequestQueue(context);
            String url = "https://agrivest.app/api/contract/receipt";
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    showMessage(
                            "success",
                            "[ONLINE] Receipt issued",
                            "Receipt number is " + response
                    );
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    saveReceipt();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cid", receiptDetails.get("id"));
                    params.put("user_id", userDetails.getString("id", ""));
                    params.put("amount", receiptDetails.get("amount"));
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
        } else {
            saveReceipt();
        }
    }

    private void saveReceipt() {
        SQLiteDatabase db = new SQLiteHelper(context).getReadableDatabase();
        ContentValues receiptValues = new ContentValues();
        receiptValues.put(SQLiteHelper.RECEIPT_CONTRACT_ID, receiptDetails.get("id"));
        receiptValues.put(SQLiteHelper.RECEIPT_CUSTOMER_NAME, receiptDetails.get("customer_name"));
        receiptValues.put(SQLiteHelper.RECEIPT_USER_ID, userDetails.getString("id", ""));
        receiptValues.put(SQLiteHelper.RECEIPT_AMOUNT, receiptDetails.get("amount"));
        long id = db.insert(SQLiteHelper.RECEIPT_TABLE_NAME, null, receiptValues);
        if (id != -1) {
            showMessage(
                    "success",
                    "[OFFLINE] Receipt issued",
                    "Please connect to internet as soon as possible."
            );
        } else {
            showMessage(
                    "failure",
                    "[OFFLINE] Receipt failed",
                    "Failed to saved offline receipt data. Please contact system administrator immediately."
            );
        }
        db.close();
    }
}
