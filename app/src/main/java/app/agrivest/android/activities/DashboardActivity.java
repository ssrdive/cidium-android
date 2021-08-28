package app.agrivest.android.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.agrivest.android.fragments.CommitmentsFragment;
import app.agrivest.android.fragments.ContractsFragment;
import app.agrivest.android.R;
import app.agrivest.android.utils.SQLiteHelper;
import app.agrivest.android.utils.Utils;
import app.agrivest.android.api.API;

public class DashboardActivity extends AppCompatActivity {
    private Utils utils;
    private boolean connected;
    private SharedPreferences userDetails;
    private RequestQueue mQueue;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private ContractsFragment contractsFragment;
    private CommitmentsFragment commitmentsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        utils = new Utils();
        connected = utils.isInternetAvailable(getApplicationContext());
        userDetails = this.getSharedPreferences("user_details", MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        contractsFragment = new ContractsFragment();
        commitmentsFragment = new CommitmentsFragment();

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(contractsFragment, "Contracts");
        viewPagerAdapter.addFragment(commitmentsFragment, "Commitments");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_people_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_comment_black_24dp);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        connected = utils.isInternetAvailable(getApplicationContext());

        uploadOfflineReceipts();
    }

    private void signOut() {
        SharedPreferences.Editor userEditor = userDetails.edit();
        userEditor.clear();
        userEditor.commit();

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
    }

    private void uploadOfflineReceipts() {
        final SQLiteDatabase db = new SQLiteHelper(getApplicationContext()).getReadableDatabase();
        if (connected) {
            final Cursor res = db.rawQuery("SELECT * FROM " + SQLiteHelper.RECEIPT_TABLE_NAME, null);
            boolean valid = res.moveToFirst();
            if (valid)
                uploadReceipt(db, res);
        }
    }

    public void uploadReceipt(final SQLiteDatabase db, final Cursor res) {
        String url = new API().getApiLink() + "/contract/receipt";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                db.delete(SQLiteHelper.RECEIPT_TABLE_NAME, "id = ?", new String[]{res.getString(res.getColumnIndex("id"))});
                res.moveToNext();
                if (res.isAfterLast() == false)
                    uploadReceipt(db, res);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    switch (networkResponse.statusCode) {
                        case 500:
                            Toast.makeText(getApplicationContext(), "Error uploading offline receipts. Please check the amounts and validate with the office", Toast.LENGTH_LONG).show();
                            break;
                        case 400:
                            signOut();
                            break;
                    }
                    Log.d("STATUS_CODE", String.valueOf(networkResponse.statusCode));
                }
                res.moveToNext();
                if (res.isAfterLast() == false)
                    uploadReceipt(db, res);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cid", res.getString(res.getColumnIndex("contract_id")));
                params.put("user_id", res.getString(res.getColumnIndex("user_id")));
                params.put("amount", res.getString(res.getColumnIndex("amount")));
                if (res.getString(res.getColumnIndex("payment_type")).equals("Cash")) {
                    params.put("due_date", "");
                } else {
                    params.put("due_date", res.getString(res.getColumnIndex("due_date")));
                }
				params.put("checksum", res.getString(res.getColumnIndex("checksum")));
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
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.offline_receipts:
                intent = new Intent(getApplicationContext(), OfflineReceiptsActivity.class);
                startActivity(intent);
                break;
            case R.id.issued_receipts:
                intent = new Intent(getApplicationContext(), IssuedReceiptsActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out:
                signOut();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
