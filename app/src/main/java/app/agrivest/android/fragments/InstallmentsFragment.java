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

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class InstallmentsFragment extends Fragment {

    Utils utils;

    private ProgressDialog loadInstallments;

    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    TableLayout installmentsTable;

    public InstallmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View installments = inflater.inflate(R.layout.fragment_installments, container, false);

        final String id = getArguments().getString("id");

        utils = new Utils();
        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(getActivity());
        installmentsTable = installments.findViewById(R.id.installments_TL);

        loadInstallments(id);

        return installments;
    }

    private void loadInstallments(String id) {
        if (utils.isInternetAvailable(getActivity())) {
            loadInstallments = new ProgressDialog(getActivity());
            loadInstallments.setTitle("Loading Installments");
            loadInstallments.setMessage("Please wait while installments are loaded");
            loadInstallments.setCancelable(false);
            String url = new API().getApiLink() + "/contract/installments/" + id;
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray installments = new JSONArray(response);
                        RowAdder rowAdder = new RowAdder(getActivity());
                        for (int i = 0; i < installments.length(); i++) {
                            JSONObject installment = installments.getJSONObject(i);
                            Iterator<String> installmentIterator = installment.keys();
                            String tableExp[] = {"id"};
                            final LinkedHashMap<String, String> contractMap = new LinkedHashMap<String, String>();
                            while (installmentIterator.hasNext()) {
                                String key = installmentIterator.next();
                                if (!Arrays.asList(tableExp).contains(key)) {
                                    contractMap.put(key, installment.getString(key));
                                }
                            }
                            rowAdder.installment(installmentsTable, contractMap);
                        }
                        loadInstallments.dismiss();
                    } catch (JSONException e) {
                        loadInstallments.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadInstallments.dismiss();
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
