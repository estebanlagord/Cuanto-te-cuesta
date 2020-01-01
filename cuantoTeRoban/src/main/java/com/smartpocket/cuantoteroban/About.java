package com.smartpocket.cuantoteroban;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

public class About extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
		
		ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		TextView appName = findViewById(R.id.appNameView);
		TextView versionTitle = findViewById(R.id.versionTitle);
		TextView versionNumber = findViewById(R.id.versionNumber);
		TextView thanksTitle = findViewById(R.id.thanksTitle);
		TextView thanksContent = findViewById(R.id.thanksContent);
		TextView notThanksTitle = findViewById(R.id.notThanksTitle);
		TextView notThanksContent = findViewById(R.id.notThanksContent);
		
		try {
			versionNumber.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			versionNumber.setText("?");
		}
		
		for (TextView view : new TextView[]{appName, thanksTitle, notThanksTitle}){
			view.setTypeface(MainActivity.TYPEFACE_ROBOTO_BLACK);
		}
		
		for (TextView view : new TextView[]{versionTitle, versionNumber, thanksContent, notThanksContent}){
			view.setTypeface(MainActivity.TYPEFACE_ROBOTO_CONDENSED_ITALIC);
		}
	}
}
