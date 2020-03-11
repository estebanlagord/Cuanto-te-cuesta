package com.smartpocket.cuantoteroban.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.smartpocket.cuantoteroban.Currency;
import com.smartpocket.cuantoteroban.R;

public class PreferencesFragmentForCurrency extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pref_with_toolbar, container, false);
		Toolbar toolbar = view.findViewById(R.id.my_awesome_toolbar);
		((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
		((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Currency currentCurrency = PreferencesManager.getInstance().getCurrentCurrency();
		ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
		actionBar.setTitle(getString(R.string.application_preferences) + " para " + currentCurrency.getCode());

		if (savedInstanceState == null) {
			getParentFragmentManager().beginTransaction().replace(R.id.content_frame, new MyPreferenceForCurrencyFragment()).commit();
		}
    }

    public static class MyPreferenceForCurrencyFragment extends PreferenceFragmentCompat
    {
    	private static final String CURRENT_VALUE = "Valor actual: ";
        private static final String CURRENCY_TOKEN = "[ZZZ]";

    	@Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // get current currency and change the preferences file name
            Currency currentCurrency = PreferencesManager.getInstance().getCurrentCurrency();
            String sharedPreferencesFileName = PreferencesManager.PREFS_NAME_CURRENCY + currentCurrency.getCode();
            getPreferenceManager().setSharedPreferencesName(sharedPreferencesFileName);
            addPreferencesFromResource(R.xml.preferences_for_currency);

            // set titles
            Preference titlePref = findPreference("currency_preference_screen");
            String newTitle = titlePref.getTitle() + currentCurrency.getCode();
            titlePref.setTitle(newTitle);

            PreferenceCategory agencyCat = findPreference("agency_category");
            String newAgencyCatTitle = agencyCat.getTitle().toString() + " (" + currentCurrency.getCode() + ")";
            agencyCat.setTitle(newAgencyCatTitle);

            PreferenceCategory bankCat = findPreference("bank_category");
            String newBankTitle = bankCat.getTitle().toString() + " (" + currentCurrency.getCode() + ")";
            bankCat.setTitle(newBankTitle);

            String newSummary = titlePref.getSummary().toString().replace(CURRENCY_TOKEN, currentCurrency.getName() + '.');
            titlePref.setSummary(newSummary);


            for(String prefKey : PreferencesManager.getInstance().getAllPreferenceKeys()){
            	Preference preference = findPreference(prefKey);
            	if (preference != null){
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
        	else if (pref instanceof CheckBoxPreference){
        		updateValueForBankExchangeRate();
        		updateSummaryForInverseConvertion();
        	}
        }

    	private void updateSummaryForInverseConvertion() {
    		final String SPACE = " ";

    		String chosenCurrencyCode = PreferencesManager.getInstance().getCurrentCurrency().getCode();
    		CheckBoxPreference invertAgencyRatePref = findPreference(PreferencesManager.AGENCY_EXCHANGE_RATE_INVERTED);
    		CheckBoxPreference invertBankRatePref = findPreference(PreferencesManager.BANK_EXCHANGE_RATE_INVERTED);

    		String summaryOn = invertAgencyRatePref.getSummaryOn().toString();
    		summaryOn = summaryOn.subSequence(0, summaryOn.lastIndexOf(SPACE) + 1).toString();
    		summaryOn += chosenCurrencyCode;
    		invertAgencyRatePref.setSummaryOn(summaryOn);
    		invertBankRatePref.setSummaryOn(summaryOn);

    		String summaryOff = invertAgencyRatePref.getSummaryOff().toString();
    		summaryOff = summaryOff.subSequence(0, summaryOff.lastIndexOf(SPACE) + 1).toString();
    		summaryOff += chosenCurrencyCode;
    		invertAgencyRatePref.setSummaryOff(summaryOff);
    		invertBankRatePref.setSummaryOff(summaryOff);
    	}

    	private void updateValueForBankExchangeRate() {
    		CheckBoxPreference isUseInternetRateEnabledPref = findPreference(PreferencesManager.USE_INTERNET_BANK_EXCHANGE_RATE);

    		// if "Automatic Updates" are enabled, we want to update the value for the Bank Exchange Rate
    		if (isUseInternetRateEnabledPref.isChecked()){

    			// find the Internet exchange rate for the new currency
    			String chosenCurrencyValue = Double.toString(PreferencesManager.getInstance().getInternetExchangeRate());
    			EditTextPreference bankExchangeRatePref = findPreference(PreferencesManager.BANK_EXCHANGE_RATE);
    			bankExchangeRatePref.setText(chosenCurrencyValue);

    			// we also want to uncheck the "Invert rate" checkbox
    			CheckBoxPreference invertBankRatePref = findPreference(PreferencesManager.BANK_EXCHANGE_RATE_INVERTED);
    			invertBankRatePref.setChecked(false);
    		}
    	}
    }
}
