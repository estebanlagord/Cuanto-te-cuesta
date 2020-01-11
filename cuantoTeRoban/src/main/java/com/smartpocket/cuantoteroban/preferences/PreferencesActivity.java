package com.smartpocket.cuantoteroban.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.smartpocket.cuantoteroban.AdViewHelper;
import com.smartpocket.cuantoteroban.Currency;
import com.smartpocket.cuantoteroban.CurrencyManager;
import com.smartpocket.cuantoteroban.R;

import java.util.ArrayList;
import java.util.List;

public class PreferencesActivity extends AppCompatActivity {

	private AdViewHelper adViewHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_toolbar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

		ViewGroup adViewContainer = findViewById(R.id.adViewContainer);
		adViewHelper = new AdViewHelper(adViewContainer, this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (adViewHelper != null) adViewHelper.resume();
	}

	@Override
	protected void onPause() {
		if (adViewHelper != null) adViewHelper.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (adViewHelper != null) adViewHelper.destroy();
		super.onDestroy();
	}

    public static class MyPreferenceFragment extends PreferenceFragmentCompat
    {
    	private static final String CURRENT_VALUE = "Valor actual: ";

		@Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(PreferencesManager.PREFS_NAME_SHARED);
            addPreferencesFromResource(R.xml.preferences);

            Preference currencyPreference = findPreference("currency_preference");
            currencyPreference.setOnPreferenceClickListener(preference -> {
				startActivity(new Intent(requireContext(), PreferencesActivityForCurrency.class));
				return true;
			});

            for(String prefKey : PreferencesManager.getInstance().getAllPreferenceKeys()){
            	Preference preference = findPreference(prefKey);
            	if (preference != null){
            		initializeChosenCurrencyListPreference(preference);
            		updateSummaryForPreference(preference, null);

            		if (preference instanceof EditTextPreference){
	    				preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	    					@Override
	    					public boolean onPreferenceClick(Preference preference) {
//	    						((EditTextPreference)preference).getEditText().selectAll(); //select all the value in the Edit Text
	    						return false;
	    					}
	    				});
            		}

    				preference.setOnPreferenceChangeListener((preference1, newValue) -> {
						updateSummaryForPreference(preference1, newValue);
						return true;
					});
            	}
            }
        }

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

		}


		private void initializeChosenCurrencyListPreference(Preference preference) {
			if (preference instanceof ListPreference) {
				List<String> entries = new ArrayList<>();
				List<String> entryValues = new ArrayList<>();
				List<Currency> chosenCurrencies = PreferencesManager.getInstance().getChosenCurrencies();

				for(Currency cur : chosenCurrencies) {
					entries.add(cur.getName());
					entryValues.add(cur.getCode());
				}

				ListPreference listPref = (ListPreference)preference;
				listPref.setEntries(entries.toArray(new String[0]));
				listPref.setEntryValues(entryValues.toArray(new String[0]));
			}
		}


        private void updateSummaryForPreference(Preference pref, Object newValue){
        	if (pref instanceof EditTextPreference) {
    	    	EditTextPreference textPref = (EditTextPreference) pref;
    	    	if (newValue==null)
    	    		newValue = textPref.getText();

    	    	String summary = textPref.getSummary().toString();
    	    	if (summary.contains("\n")){
    	    		int index = summary.lastIndexOf('\n');
    	    		summary = summary.substring(0, index);
    	    	}

    	    	summary += "\n" + CURRENT_VALUE + newValue;
    	    	pref.setSummary(summary);
        	}
        	else if (pref instanceof ListPreference){
        		// update current value for "Moneda a convertir"
        		ListPreference listPref = (ListPreference) pref;
        		if (newValue==null)
        			newValue = PreferencesManager.getInstance().getCurrentCurrency().getCode();

        		Currency newCurr = CurrencyManager.getInstance().findCurrency(newValue.toString());
       			PreferencesManager.getInstance().setCurrentCurrency(newCurr);

        		listPref.setSummary(CURRENT_VALUE + newCurr.getName());
        	}
        }
    }
}
