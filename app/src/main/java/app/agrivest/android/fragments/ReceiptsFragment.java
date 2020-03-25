package app.agrivest.android.fragments;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import app.agrivest.android.R;
import app.agrivest.android.api.API;
import app.agrivest.android.utils.RowAdder;
import app.agrivest.android.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReceiptsFragment extends Fragment {

    Utils utils;

    private ProgressDialog loadReceipts;

    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    TableLayout receiptsTable;


    public ReceiptsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View receipts = inflater.inflate(R.layout.fragment_receipts, container, false);

        final String id = getArguments().getString("id");

        utils = new Utils();
        userDetails = getActivity().getSharedPreferences("user_details", getActivity().MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(getActivity());
        receiptsTable = receipts.findViewById(R.id.receipts_TL);
        loadReceipts(id);

        return receipts;
    }

    private void loadReceipts(String id) {
        if(utils.isInternetAvailable(getActivity())) {
            loadReceipts = new ProgressDialog(getActivity());
            loadReceipts.setTitle("Loading receipts");
            loadReceipts.setMessage("Please wait while receipts are loaded");
            loadReceipts.setCancelable(false);
            String url =  new API().getApiLink() + "/contract/receipts/" + id;
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray installments = new JSONArray(response);
                        RowAdder rowAdder = new RowAdder(getActivity());
                        for (int i = 0; i < installments.length(); i++) {
                            JSONObject installment = installments.getJSONObject(i);
                            Iterator<String> installmentIterator = installment.keys();
                            String tableExp[] = {};
                            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                            while (installmentIterator.hasNext()) {
                                String key = installmentIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    if(key.equals("notes")) {
                                        contractMap.put(key, new JSONObject(installment.getString(key)).getString("String"));
                                    } else {
                                        contractMap.put(key, installment.getString(key));
                                    }
                                }
                            }
                            rowAdder.receipt(receiptsTable, contractMap);
                        }
                        loadReceipts.dismiss();
                    } catch (JSONException e) {
                        loadReceipts.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadReceipts.dismiss();
                    error.printStackTrace();
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
        }
    }
}
