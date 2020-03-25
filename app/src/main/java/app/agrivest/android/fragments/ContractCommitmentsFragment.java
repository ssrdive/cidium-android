package app.agrivest.android.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import app.agrivest.android.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContractCommitmentsFragment extends Fragment {


    public ContractCommitmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contractCommitments = inflater.inflate(R.layout.fragment_contract_commitments, container, false);


        return contractCommitments;
    }

}
