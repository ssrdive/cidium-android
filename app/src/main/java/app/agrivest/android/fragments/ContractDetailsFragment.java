package app.agrivest.android.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import app.agrivest.android.R;
import app.agrivest.android.activities.ReceiptActivity;
import app.agrivest.android.utils.NumberFormatter;
import app.agrivest.android.utils.SQLiteHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContractDetailsFragment extends Fragment {

    TextView id_TV;
    TextView customer_name_TV;
    TextView customer_address_TV;
    TextView model_TV;
    TextView amount_pending_TV;
    MaterialButton total_payable_TV;
    TextView chassis_number_TV;
    TextView contact_TV;

    public ContractDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contractDetails = inflater.inflate(R.layout.fragment_contract_details, container, false);

        final String id = getArguments().getString("id");

        SQLiteDatabase db = new SQLiteHelper(getActivity()).getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.CONTRACT_TABLE_NAME + " WHERE id = " + id, null);
        res.moveToFirst();

        final String agrivest = res.getString(res.getColumnIndex("agrivest"));
        final String customer_name = res.getString(res.getColumnIndex("customer_name"));
        final String customer_address = res.getString(res.getColumnIndex("customer_address"));
        final String model = res.getString(res.getColumnIndex("model"));
        final String customer_contact = res.getString(res.getColumnIndex("customer_contact"));
        final String chassis_number = res.getString(res.getColumnIndex("chassis_number"));
        final String amount_pending = res.getString(res.getColumnIndex("amount_pending"));
        final String total_payable = res.getString(res.getColumnIndex("total_payable"));

        db.close();

        id_TV = contractDetails.findViewById(R.id.id_TV);
        customer_name_TV = contractDetails.findViewById(R.id.customer_name_TV);
        customer_address_TV = contractDetails.findViewById(R.id.customer_address_TV);
        model_TV = contractDetails.findViewById(R.id.model_TV);
        contact_TV = contractDetails.findViewById(R.id.contact_TV);
        chassis_number_TV = contractDetails.findViewById(R.id.chassis_number_TV);
        amount_pending_TV = contractDetails.findViewById(R.id.amount_pending_TV);
        total_payable_TV = contractDetails.findViewById(R.id.total_payable_TV);

        total_payable_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceiptActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id", id);
                getActivity().startActivity(intent);
            }
        });

        id_TV.setText(id);
        customer_name_TV.setText(customer_name);
        customer_address_TV.setText(customer_address);
        model_TV.setText(model);
        chassis_number_TV.setText(chassis_number);

        NumberFormatter formatter = new NumberFormatter();

        amount_pending_TV.setText(formatter.format(amount_pending));
        if (Float.parseFloat(amount_pending) > 0) {
            amount_pending_TV.setTextColor(Color.parseColor("#b03428"));
        } else {
            amount_pending_TV.setTextColor(Color.parseColor("#196912"));
        }
        total_payable_TV.setText(formatter.format(total_payable));

        contact_TV.setText(customer_contact);
        contact_TV.setClickable(true);
        contact_TV.setTextColor(Color.parseColor("#3269a8"));
        contact_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tel://" + customer_contact)));
            }
        });

        return contractDetails;
    }

}
