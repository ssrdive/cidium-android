package app.agrivest.android.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import app.agrivest.android.fragments.ContractCommitmentsFragment;
import app.agrivest.android.fragments.ContractDetailsFragment;
import app.agrivest.android.fragments.InstallmentsFragment;
import app.agrivest.android.R;
import app.agrivest.android.fragments.ReceiptsFragment;
import app.agrivest.android.utils.Utils;

public class ContractDetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private ContractDetailsFragment contractDetailsFragment;
    private InstallmentsFragment installmentsFragment;
    private ReceiptsFragment receiptsFragment;
    private ContractCommitmentsFragment contractCommitmentsFragment;

    Utils utils;



    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    TableLayout receiptsTable;
    TableLayout installmentsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        Bundle bundle = getIntent().getExtras();
        Bundle fgBundle = new Bundle();
        fgBundle.putString("id", bundle.getString("id"));

        contractDetailsFragment = new ContractDetailsFragment();
        contractDetailsFragment.setArguments(fgBundle);
        installmentsFragment = new InstallmentsFragment();
        installmentsFragment.setArguments(fgBundle);
        receiptsFragment = new ReceiptsFragment();
        receiptsFragment.setArguments(fgBundle);
        contractCommitmentsFragment = new ContractCommitmentsFragment();
        contractCommitmentsFragment.setArguments(fgBundle);

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(contractDetailsFragment, "Details");
        viewPagerAdapter.addFragment(installmentsFragment, "Installments");
        viewPagerAdapter.addFragment(receiptsFragment, "Receipts");
        viewPagerAdapter.addFragment(contractCommitmentsFragment, "Commitments");
        viewPager.setAdapter(viewPagerAdapter);
//
        getSupportActionBar().setTitle("Contract Details ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        utils = new Utils();
//        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
//        mQueue = Volley.newRequestQueue(this);
//        receiptsTable = findViewById(R.id.receipts_TL);
//        installmentsTable = findViewById(R.id.installments_TL);
//        loadReceipts(id);
//        loadInstallments(id);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

}
