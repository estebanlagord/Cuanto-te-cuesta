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

public class About extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container, false);
		Toolbar toolbar = view.findViewById(R.id.my_awesome_toolbar);
		((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
		((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		TextView appName = view.findViewById(R.id.appNameView);
		TextView versionTitle = view.findViewById(R.id.versionTitle);
		TextView versionNumber = view.findViewById(R.id.versionNumber);
		TextView thanksTitle = view.findViewById(R.id.thanksTitle);
		TextView thanksContent = view.findViewById(R.id.thanksContent);
		TextView notThanksTitle = view.findViewById(R.id.notThanksTitle);
		TextView notThanksContent = view.findViewById(R.id.notThanksContent);
		
		try {
			versionNumber.setText(requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			versionNumber.setText("?");
		}
		
		for (TextView tv : new TextView[]{appName, thanksTitle, notThanksTitle}){
			tv.setTypeface(MainActivity.TYPEFACE_ROBOTO_BLACK);
		}
		
		for (TextView tv : new TextView[]{versionTitle, versionNumber, thanksContent, notThanksContent}){
			tv.setTypeface(MainActivity.TYPEFACE_ROBOTO_CONDENSED_ITALIC);
		}
	}
}
