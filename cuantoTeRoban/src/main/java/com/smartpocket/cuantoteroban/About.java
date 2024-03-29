package com.smartpocket.cuantoteroban;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.smartpocket.cuantoteroban.databinding.AboutBinding;

public class About extends Fragment {
    private AboutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AboutBinding.inflate(inflater);
        Toolbar toolbar = binding.getRoot().findViewById(R.id.my_awesome_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView versionNumber = binding.versionNumber;

        try {
            versionNumber.setText(requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            versionNumber.setText("?");
        }
    }
}
