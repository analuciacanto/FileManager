package com.urmobo.filemanager.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.urmobo.filemanager.R;

public class SDCardFragment extends Fragment {
    View view;

    @Nullable
   // @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container){
        view = inflater.inflate(R.layout.fragment_sd_card, container, false);

        return view;
    }

}
