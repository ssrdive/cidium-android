package app.agrivest.android.fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
public class ContractCommitmentsFragment extends Fragment implements View.OnClickListener {
    Utils utils;
    String id;
    private ProgressDialog loadInstallments;
    private ProgressDialog commitmentAction;
    private LinearLayout commitmentsLayout;
    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    private EditText text;
    private EditText dueDate;
    private MaterialButton add;

    View contractCommitments;


    public ContractCommitmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contractCommitments = inflater.inflate(R.layout.fragment_contract_commitments, container, false);

        id = getArguments().getString("id");

        utils = new Utils();
        userDetails = getActivity().getSharedPreferences("user_details", MODE_PRIVATE);
        commitmentsLayout = contractCommitments.findViewById(R.id.commitments_layout);
        text = contractCommitments.findViewById(R.id.text);
        dueDate = contractCommitments.findViewById(R.id.due_date);
        dueDate.setInputType(InputType.TYPE_NULL);
        add = contractCommitments.findViewById(R.id.add);
        mQueue = Volley.newRequestQueue(getActivity());

        add.setOnClickListener(this);
        dueDate.setOnClickListener(this);

        loadCommitments(id);

        return contractCommitments;
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

    private void commitmentAction(final String id, final String cid, final String fulfilled) {
        if (utils.isInternetAvailable(getActivity())) {
            commitmentAction = new ProgressDialog(getActivity());
            commitmentAction.setTitle("Updating commitment");
            commitmentAction.setMessage("Please wait while commitment is updated");
            commitmentAction.setCancelable(false);
            String url = new API().getApiLink() + "/contract/commitment/action";
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getActivity(), "Commitment updated", Toast.LENGTH_SHORT).show();
                    loadCommitments(id);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    commitmentAction.dismiss();
                    Toast.makeText(getActivity(), "Failed to update commitment", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("id", cid);
                    params.put("fulfilled", fulfilled);
                    params.put("user", userDetails.getString("id", ""));
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
        }
    }

    private void addCommitment(final String id, final String text, final String due_date) {
        if (utils.isInternetAvailable(getActivity())) {
            commitmentAction = new ProgressDialog(getActivity());
            commitmentAction.setTitle("Adding commitment");
            commitmentAction.setMessage("Please wait while commitment is added");
            commitmentAction.setCancelable(false);
            String url = new API().getApiLink() + "/contract/commitment";
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getActivity(), "Commitment added", Toast.LENGTH_SHORT).show();
                    loadCommitments(id);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    commitmentAction.dismiss();
                    Toast.makeText(getActivity(), "Failed to add commitment", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("contract_id", id);
                    params.put("text", text);
                    params.put("due_date", due_date);
                    params.put("user_id", userDetails.getString("id", ""));
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
        }
    }

    private void loadCommitments(final String id) {
        if (utils.isInternetAvailable(getActivity())) {
            loadInstallments = new ProgressDialog(getActivity());
            loadInstallments.setTitle("Loading Commitments");
            loadInstallments.setMessage("Please wait while commitments are loaded");
            loadInstallments.setCancelable(false);
            String url = new API().getApiLink() + "/contract/commitments/" + id;
            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        commitmentsLayout.removeAllViews();
                        response = fixEncoding(response);
                        JSONArray installments = new JSONArray(response);
                        for (int i = 0; i < installments.length(); i++) {
                            final JSONObject commitment = installments.getJSONObject(i);

                            String commitmentTitleHead = "";

                            if (commitment.getInt("commitment") == 1) {
                                if (commitment.getJSONObject("fulfilled").getBoolean("Valid")) {
                                    if (commitment.getJSONObject("fulfilled").getInt("Int32") == 1) {
                                        // Success commitment
                                        commitmentTitleHead = "<font color='#3ea85b'><b>Commitment</b></font> ";
                                    } else {
                                        // Danger commitment
                                        commitmentTitleHead = "<font color='#a83e57'><b>Commitment</b></font> ";
                                    }
                                } else {
                                    if (commitment.getJSONObject("due_in").getInt("Int32") <= 0) {
                                        // Expired
                                        commitmentTitleHead = "<font color='#a83e57'><b>Commitment</b></font> Expired <font color='#a83e57'><b>"
                                                + Math.abs(commitment.getJSONObject("due_in").getInt("Int32"))
                                                + " days</b></font> ago ";
                                    } else {
                                        // Upcoming
                                        commitmentTitleHead = "<font color='#3ea85b'><b>Commitment</b></font> Expires in <font color='#3ea85b'><b>"
                                                + Math.abs(commitment.getJSONObject("due_in").getInt("Int32"))
                                                + " days</b></font> ";
                                    }
                                }
                            } else {
                                commitmentTitleHead = "<font color='blue'><b>Comment</b></font> ";
                            }

                            commitmentTitleHead += commitment.getString("created_by") + " added on "
                                    + commitment.getString("created");

                            TextView tv = new TextView(getActivity());
                            tv.setText(Html.fromHtml(commitmentTitleHead));
                            commitmentsLayout.addView(tv);

                            if (commitment.getJSONObject("fulfilled").getBoolean("Valid") || commitment.getInt("commitment") == 0) {

                            } else {
                                Button fulfilled = new Button(getActivity());
                                fulfilled.setText(Html.fromHtml("<font color='#3ea85b'>Fulfilled</font>"));
                                fulfilled.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            commitmentAction(id, commitment.getString("id"), "1");
                                        } catch (JSONException e) {

                                        }
                                    }
                                });
                                Button broken = new Button(getActivity());
                                broken.setText(Html.fromHtml("<font color='#a83e57'>Broken</font>"));
                                broken.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            commitmentAction(id, commitment.getString("id"), "0");
                                        } catch (JSONException e) {

                                        }
                                    }
                                });

                                LinearLayout buttonsLayout = new LinearLayout(getActivity(), null, R.style.CommitmentButtonsLayout);
                                buttonsLayout.addView(fulfilled);
                                buttonsLayout.addView(broken);

                                commitmentsLayout.addView(buttonsLayout);
                            }

                            tv = new TextView(getActivity());
                            tv.setText(commitment.getString("text"));
                            commitmentsLayout.addView(tv);

                            TextView separator = new TextView(getActivity());
                            commitmentsLayout.addView(separator);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.due_date:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        dueDate.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                }, utils.getYear(), utils.getMonth() - 1, utils.getDay());
                datePickerDialog.show();
                break;
            case R.id.add:
                String textInput = text.getText().toString();
                if(textInput.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter the text", Toast.LENGTH_SHORT).show();
                    return;
                }
                String dueDateInput = dueDate.getText().toString();
                addCommitment(id, textInput, dueDateInput);
                break;
        }
    }
}
