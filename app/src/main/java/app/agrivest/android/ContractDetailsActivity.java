package app.agrivest.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ContractDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_details);

        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");
    }
}
