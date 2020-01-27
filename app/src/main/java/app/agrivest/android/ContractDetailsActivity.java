package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ContractDetailsActivity extends AppCompatActivity {
    TextView id_TV;
    TextView customer_name_TV;
    TextView model_TV;
    TextView amount_pending_TV;
    TextView total_payable_TV;
    TextView chassis_number_TV;
    TextView contact_TV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_details);

        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");
        String customer_name = bundle.getString("customer_name");
        String model = bundle.getString("model");
        final String customer_contact = bundle.getString("customer_contact");
        String chassis_number = bundle.getString("chassis_number");
        String amount_pending = bundle.getString("amount_pending");
        String total_payable = bundle.getString("total_payable");

        id_TV = findViewById(R.id.id_TV);
        customer_name_TV = findViewById(R.id.customer_name_TV);
        model_TV = findViewById(R.id.model_TV);
        contact_TV = findViewById(R.id.contact_TV);
        chassis_number_TV = findViewById(R.id.chassis_number_TV);
        amount_pending_TV = findViewById(R.id.amount_pending_TV);
        total_payable_TV = findViewById(R.id.total_payable_TV);

        id_TV.setText(id);
        customer_name_TV.setText(customer_name);
        model_TV.setText(model);
        chassis_number_TV.setText(chassis_number);

        NumberFormatter formatter = new NumberFormatter();

        amount_pending_TV.setText(formatter.format(amount_pending));
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

        getSupportActionBar().setTitle("Contract Details " + id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
