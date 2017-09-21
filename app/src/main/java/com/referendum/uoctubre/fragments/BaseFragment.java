package com.referendum.uoctubre.fragments;

import android.support.v4.app.Fragment;


public class BaseFragment extends Fragment {
    protected boolean isFragmentSafe() {
        return getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed();
    }
}
