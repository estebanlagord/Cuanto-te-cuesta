package com.smartpocket.cuantoteroban.preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.CheckBoxPreference;
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

import static com.smartpocket.cuantoteroban.preferences.PreferencesManager.THEME_CLEAR;
import static com.smartpocket.cuantoteroban.preferences.PreferencesManager.THEME_DARK;
import static com.smartpocket.cuantoteroban.preferences.PreferencesManager.THEME_SYSTEM_DEFAULT;

public class PreferencesFragment extends Fragment {

    private static final String[] themeOptions = {"Claro", "Oscuro", "Usar configuración de Android"};
    private static final String[] themeOptionsValues = {THEME_CLEAR, THEME_DARK, THEME_SYSTEM_DEFAULT};

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
        if (savedInstanceState == null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MyPreferenceFragment()).commit();
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        private static final String CURRENT_VALUE = "Valor actual: ";
        private SingleActivityVM singleActivityVM;
        private final PreferencesManager preferences = PreferencesManager.getInstance();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            singleActivityVM = new ViewModelProvider(requireActivity()).get(SingleActivityVM.class);

            getPreferenceManager().setSharedPreferencesName(PreferencesManager.PREFS_NAME_SHARED);
            addPreferencesFromResource(R.xml.preferences);

            CheckBoxPreference showDiscountTaxPreference = findPreference(PreferencesManager.SHOW_DISCOUNT);
            showDiscountTaxPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals(false)) {
                    preferences.setDiscount(0.0);
                    preferences.setTaxes(0.0);
                }
                return true;
            });

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

            Preference writeReview = findPreference("send_review");
            writeReview.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.google_play_app_url)));
                intent.setPackage("com.android.vending");
                startActivity(intent);
                return true;
            });

            Preference sendEmail = findPreference("email_developer");
            sendEmail.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"estebanlagord+Cuanto.Te.Cuesta.developer@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "App ¿Cuanto Te Cuesta?");
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            });

            Preference chooseTheme = findPreference("choose_theme");
            chooseTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                String value = (String) newValue;
                int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                switch (value) {
                    case THEME_CLEAR:
                        nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case THEME_DARK:
                        nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                }
                AppCompatDelegate.setDefaultNightMode(nightMode);
                return true;
            });

            for (String prefKey : PreferencesManager.getInstance().getAllPreferenceKeys()) {
                Preference preference = findPreference(prefKey);
                if (preference != null) {
                    initializeChosenCurrencyListPreference(preference);
                    initializeChooseThemeListPreference(preference);
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

                    if (preference.getOnPreferenceChangeListener() == null) {
                        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                            updateSummaryForPreference(preference1, newValue);
                            return true;
                        });
                    }
                }
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }


        private void initializeChosenCurrencyListPreference(Preference preference) {
            if (preference instanceof ListPreference && preference.getKey().equals("source_currency")) {
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

        private void initializeChooseThemeListPreference(Preference preference) {
            if (preference instanceof ListPreference && preference.getKey().equals("choose_theme")) {
                ListPreference listPref = (ListPreference) preference;
                listPref.setEntries(themeOptions);
                listPref.setEntryValues(themeOptionsValues);
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
            } else if (pref.getKey().equals("source_currency")) {
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
