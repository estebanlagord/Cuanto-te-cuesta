package com.smartpocket.cuantoteroban.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.smartpocket.cuantoteroban.BuildConfig;
import com.smartpocket.cuantoteroban.Currency;
import com.smartpocket.cuantoteroban.CurrencyManager;
import com.smartpocket.cuantoteroban.R;
import com.smartpocket.cuantoteroban.SingleActivityVM;

import java.util.ArrayList;
import java.util.List;

public class PreferencesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pref_with_toolbar, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitle(R.string.application_preferences);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        private static final String CURRENT_VALUE = "Valor actual: ";
        private SingleActivityVM singleActivityVM;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            singleActivityVM = ViewModelProviders.of(requireActivity()).get(SingleActivityVM.class);

            getPreferenceManager().setSharedPreferencesName(PreferencesManager.PREFS_NAME_SHARED);
            addPreferencesFromResource(R.xml.preferences);

            Preference currencyPreference = findPreference("currency_preference");
            currencyPreference.setOnPreferenceClickListener(preference -> {
                NavHostFragment.findNavController(MyPreferenceFragment.this)
                        .navigate(PreferencesFragmentDirections.actionPreferencesFragmentToMyPreferenceForCurrencyFragment());
                return true;
            });

            Preference removeAdsPreference = findPreference("remove_ads");
            removeAdsPreference.setOnPreferenceClickListener(preference -> {
                singleActivityVM.getLaunchPurchaseLD().postValue(true);
                return true;
            });

            Preference showAdsPreference = findPreference("consume_remove_ads");
            if (BuildConfig.DEBUG) {
                // DEBUG MODE, SHOW OPTION TO CONSUME
                showAdsPreference.setOnPreferenceClickListener(preference -> {
                    singleActivityVM.getLaunchRestoreAdsLD().postValue(true);
                    return true;
                });
            } else {
                // PROD MODE, HIDE OPTION TO CONSUME
                showAdsPreference.setVisible(false);
                showAdsPreference.setEnabled(false);
            }

            for (String prefKey : PreferencesManager.getInstance().getAllPreferenceKeys()) {
                Preference preference = findPreference(prefKey);
                if (preference != null) {
                    initializeChosenCurrencyListPreference(preference);
                    updateSummaryForPreference(preference, null);

                    if (preference instanceof EditTextPreference) {
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

                for (Currency cur : chosenCurrencies) {
                    entries.add(cur.getName());
                    entryValues.add(cur.getCode());
                }

                ListPreference listPref = (ListPreference) preference;
                listPref.setEntries(entries.toArray(new String[0]));
                listPref.setEntryValues(entryValues.toArray(new String[0]));
            }
        }


        private void updateSummaryForPreference(Preference pref, Object newValue) {
            if (pref instanceof EditTextPreference) {
                EditTextPreference textPref = (EditTextPreference) pref;
                if (newValue == null)
                    newValue = textPref.getText();

                String summary = textPref.getSummary().toString();
                if (summary.contains("\n")) {
                    int index = summary.lastIndexOf('\n');
                    summary = summary.substring(0, index);
                }

                summary += "\n" + CURRENT_VALUE + newValue;
                pref.setSummary(summary);
            } else if (pref instanceof ListPreference) {
                // update current value for "Moneda a convertir"
                ListPreference listPref = (ListPreference) pref;
                if (newValue == null)
                    newValue = PreferencesManager.getInstance().getCurrentCurrency().getCode();

                Currency newCurr = CurrencyManager.getInstance().findCurrency(newValue.toString());
                PreferencesManager.getInstance().setCurrentCurrency(newCurr);

                listPref.setSummary(CURRENT_VALUE + newCurr.getName());
            }
        }
    }
}
