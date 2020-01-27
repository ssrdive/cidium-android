package app.agrivest.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.widget.ProgressBar;

import com.bxl.config.editor.BXLConfigLoader;

import java.util.HashMap;
import android.os.Handler;

import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;

public class PrintReceipt implements Runnable {
    Thread thrd;
    Context context;
    ProgressDialog progressDialog;
    HashMap<String, String> receiptDetails;
    BXLConfigLoader bxlConfigLoader;
    Utils utils;
    AlertDialog printReceiptStatus;

    PrintReceipt(Context context, ProgressDialog progressDialog, HashMap<String, String> receiptDetails) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.receiptDetails = receiptDetails;
        utils = new Utils();
        thrd = new Thread(this);
        thrd.start();
    }

    @Override
    public void run() {
        Handler mainHandler = new Handler(context.getMainLooper());
        try {
            bxlConfigLoader = new BXLConfigLoader(context);
            bxlConfigLoader.openFile();
            bxlConfigLoader.saveFile();
            POSPrinter posPrinter = new POSPrinter(context);
            posPrinter.open("SPP-R210");
            posPrinter.claim(5000);
            posPrinter.setDeviceEnabled(true);
            posPrinter.checkHealth(JposConst.JPOS_CH_INTERNAL);
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Agrivest (Private) Limited\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "--------------------------------\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Customer Name " + receiptDetails.get("customer_name") + "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Chassis Number " + receiptDetails.get("chassis_number") + "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "Amount LKR " + receiptDetails.get("amount") + "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n");
            posPrinter.close();

            if (utils.isInternetAvailable(context)) {
                progressDialog.dismiss();
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        printReceiptStatus = new AlertDialog.Builder(context).create();
                        printReceiptStatus.setTitle("Receipt issued");
                        printReceiptStatus.setMessage("Receipt issued successfully.");
                        printReceiptStatus.setIcon(context.getResources().getDrawable(R.drawable.success_icon));
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
            } else {
                progressDialog.dismiss();
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        printReceiptStatus = new AlertDialog.Builder(context).create();
                        printReceiptStatus.setTitle("Receipt saved to offline receipts");
                        printReceiptStatus.setMessage("You do not currently have an active internet connection. Therefore this receipt was saved as an offline receipt. You should connect to internet and upload this receipt at your earliest convenince.");
                        printReceiptStatus.setIcon(context.getResources().getDrawable(R.drawable.success_icon));
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
        }catch(JposException e) {
            progressDialog.dismiss();
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    printReceiptStatus = new AlertDialog.Builder(context).create();
                    printReceiptStatus.setTitle("Printer error");
                    printReceiptStatus.setMessage("Something wrong with the printer. Please make sure the status light is green and the error light is not on.");
                    printReceiptStatus.setIcon(context.getResources().getDrawable(R.drawable.failure_icon));
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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
