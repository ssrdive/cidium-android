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
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
public class CommitmentsFragment extends Fragment implements View.OnClickListener {
    private ProgressDialog loadCommitments;
    private Utils utils;
    private SharedPreferences userDetails;
    private RequestQueue mQueue;
    private TableLayout commitmentsTable;

    SwitchMaterial commitmentsToggle;

    public CommitmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View commitments = inflater.inflate(R.layout.fragment_commitments, container, false);

        utils = new Utils();
        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(getActivity());
        commitmentsTable = commitments.findViewById(R.id.commitments_table);
        commitmentsToggle = commitments.findViewById(R.id.commitments_toggle);

        commitmentsToggle.setOnClickListener(this);

        loadCommitments("expired");

        return commitments;
    }

    public static String fixEncoding(String response) {
        try {
            byte[] u = response.getBytes(
                    "ISO-8859-1");
            response = new String(u, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private void loadCommitments(final String type) {
        if (utils.isInternetAvailable(getActivity())) {
            loadCommitments = new ProgressDialog(getActivity());

            while (commitmentsTable.getChildCount() > 1)
                commitmentsTable.removeView(commitmentsTable.getChildAt(commitmentsTable.getChildCount() - 1));

            loadCommitments.setTitle("Loading Commitments");
            loadCommitments.setMessage("Please wait while commitments are loaded");
            loadCommitments.setCancelable(false);
            String url = new API().getApiLink() + "/dashboard/commitments/" + type + "/" + userDetails.getString("id", "");
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        response = fixEncoding(response);
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
                            rowAdder.commitment(commitmentsTable, contractMap, type);
                        }
                        loadCommitments.dismiss();
                    } catch (JSONException e) {
                        loadCommitments.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadCommitments.dismiss();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commitments_toggle:
                if (commitmentsToggle.isChecked()) {
                    loadCommitments("upcoming");
                } else {
                    loadCommitments("expired");
                }
                break;
        }
    }
}
