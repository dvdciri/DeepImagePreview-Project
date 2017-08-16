package com.davidecirillo.menupreview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.davidecirillo.menupreview.preference.PreferenceManager;
import com.davidecirillo.menupreview.preference.PreferenceManagerImpl;


public class BaseFragment extends Fragment {

    protected PreferenceManager mPreferenceManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferenceManager = new PreferenceManagerImpl(getContext());
    }
}
