package com.smartpocket.cuantoteroban.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.billingclient.api.BillingClient;
import com.smartpocket.cuantoteroban.AdViewHelper;
import com.smartpocket.cuantoteroban.BillingHelperKt;
import com.smartpocket.cuantoteroban.BillingHelperStatusListener;
import com.smartpocket.cuantoteroban.BuildConfig;
import com.smartpocket.cuantoteroban.Currency;
import com.smartpocket.cuantoteroban.CurrencyManager;
import com.smartpocket.cuantoteroban.MyApplication;
import com.smartpocket.cuantoteroban.R;
import com.smartpocket.cuantoteroban.Utilities;

import java.util.ArrayList;
import java.util.List;

public class PreferencesActivity extends AppCompatActivity implements BillingHelperStatusListener {

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
        boolean isAdFree = MyApplication.Companion.billingHelper().isRemoveAdsPurchased();
        adViewHelper.resume(isAdFree);
    }

    @Override
    protected void onPause() {
        adViewHelper.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        adViewHelper.destroy();
        super.onDestroy();
    }

    @Override
    public void onBillingHelperStatusChanged(int code) {
        @StringRes int msg;
        if (code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            msg = R.string.billing_status_already_owned;
            adViewHelper.destroy();
        } else if (code == BillingClient.BillingResponseCode.OK) {
            msg = R.string.billing_status_thanks_for_buying;
            adViewHelper.destroy();
        } else if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
            msg = R.string.billing_status_canceled;
        } else if (code == BillingClient.BillingResponseCode.ERROR) {
            msg = R.string.billing_status_error;
        } else if (code == BillingHelperKt.PURCHASE_STATE_PENDING) {
            msg = R.string.billing_status_pending;
        } else {
            msg = R.string.billing_status_connection_error;
        }
        Utilities.showToast(getString(msg));
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        private static final String CURRENT_VALUE = "Valor actual: ";

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(PreferencesManager.PREFS_NAME_SHARED);
            addPreferencesFromResource(R.xml.preferences);

            Preference currencyPreference = findPreference("currency_preference");
            currencyPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), PreferencesActivityForCurrency.class));
                return true;
            });

            Preference removeAdsPreference = findPreference("remove_ads");
            removeAdsPreference.setOnPreferenceClickListener(preference -> {
                MyApplication.Companion.billingHelper().launchBillingFlow(requireActivity(), (BillingHelperStatusListener) requireActivity());
                return true;
            });

            Preference showAdsPreference = findPreference("consume_remove_ads");
            if (BuildConfig.DEBUG) {
                // DEBUG MODE, SHOW OPTION TO CONSUME
                showAdsPreference.setOnPreferenceClickListener(preference -> {
                    MyApplication.Companion.billingHelper().consumeRemoveAdsPurchase();
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
