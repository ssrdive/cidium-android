package app.agrivest.android.threads;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.bxl.config.editor.BXLConfigLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import app.agrivest.android.utils.NumberFormatter;
import app.agrivest.android.R;
import app.agrivest.android.utils.Utils;
import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

public class PrintReceiptSummaryThread implements Runnable {
    Thread thrd;
    Context context;
    ProgressDialog progressDialog;
    JSONArray receipts;
    Utils utils;
    AlertDialog printReceiptStatus;
    Handler mainHandler;
    SharedPreferences userDetails;

    public PrintReceiptSummaryThread(Context context, ProgressDialog progressDialog, JSONArray receipts) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.receipts = receipts;
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

        if(receipts == null || receipts.length() == 0) {
            progressDialog.dismiss();
            showMessage(
                    "Failure",
                    "No receipts issued",
                    "You do not have any receipts issued today."
            );
            return;
        }

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
            userDetails = context.getSharedPreferences("user_details", context.MODE_PRIVATE);
            POSPrinter posPrinter = new POSPrinter(context);
            posPrinter.open(BXLConfigLoader.PRODUCT_NAME_SPP_R210);
            posPrinter.claim(5000);
            posPrinter.setDeviceEnabled(true);
            posPrinter.checkHealth(JposConst.JPOS_CH_INTERNAL);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) 80);
            buffer.put((byte) 0x01);
            buffer.put((byte) 0x00);
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            Bitmap payment_confirmation_customer_information = BitmapFactory.decodeResource(context.getResources(), R.drawable.payment_confirmation_customer_information);
            posPrinter.printBitmap(buffer.getInt(0), logo, 200, POSPrinterConst.PTR_BM_CENTER);
            String ESCAPE_CHARACTERS = new String(new byte[]{0x1b, 0x7c});
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "N" + ESCAPE_CHARACTERS + "cA" + "\nAgrivest (Private) Limited\n" +
                    "Hospital Junction, Polonnaruwa\n " +
                    "027 222 22279\n");
            NumberFormatter numberFormatter = new NumberFormatter();
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "lA" + ESCAPE_CHARACTERS + "N"
                    + "________________________________\n\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "bC" + ESCAPE_CHARACTERS + "cA" + "RECEIPT SUMMARY" + "\n\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "lA" + ESCAPE_CHARACTERS + "N"
                    + java.text.DateFormat.getDateTimeInstance().format(new Date()) + "\n" + userDetails.getString("name", "") + "\n\n");

            try {
                double total = 0;
                for (int i = 0; i < receipts.length(); i++) {
                    JSONObject receipt = receipts.getJSONObject(i);
                    float amount = Float.parseFloat(receipt.getString("amount"));
                    total += amount;
                    posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "lA" + ESCAPE_CHARACTERS + "N"
                            + receipt.getString("id") + "\tRS. " + numberFormatter.format(amount) + "\n");
                }
                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "N" + ESCAPE_CHARACTERS + "cA"
                        + "\nTotal\n");
                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, ESCAPE_CHARACTERS + "4C" + ESCAPE_CHARACTERS + "cA"
                        + "RS. " + numberFormatter.format(total) + "\n\n\n");
            } catch (JSONException e) {
                e.printStackTrace();
                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n");
                posPrinter.close();
                progressDialog.dismiss();
                showMessage(
                        "Failure",
                        "Something went wrong",
                        "Something went wrong."
                );
                return;
            }

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n\n");
            posPrinter.close();
            progressDialog.dismiss();
            showMessage(
                    "success",
                    "Printed",
                    "Receipt summary printed."
            );
            return;
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
        }
    }
}
