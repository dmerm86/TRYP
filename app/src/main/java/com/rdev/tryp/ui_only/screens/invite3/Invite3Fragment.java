package com.rdev.tryp.ui_only.screens.invite3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rdev.tryp.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Invite3Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite3, container, false);
    }
}