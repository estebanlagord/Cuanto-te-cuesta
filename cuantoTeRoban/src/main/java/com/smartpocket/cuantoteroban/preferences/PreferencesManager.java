package com.smartpocket.cuantoteroban.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.smartpocket.cuantoteroban.AmountTextWatcher;
import com.smartpocket.cuantoteroban.Currency;
import com.smartpocket.cuantoteroban.CurrencyManager;
import com.smartpocket.cuantoteroban.MyApplication;
import com.smartpocket.cuantoteroban.editortype.EditorType;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreferencesManager {
    private static final PreferencesManager instance = new PreferencesManager();
    private static final String PREFS_NAME_BASE = "CuantoTeRobanPreferences";
    public static final String PREFS_NAME_SHARED = PREFS_NAME_BASE + "_Shared";
    public static final String PREFS_NAME_CURRENCY = PREFS_NAME_BASE + "_";

    private static final String DISCOUNT = "discount";
    private static final String TAXES = "taxes";

    private static final String INTERNET_EXCHANGE_RATE = "internet_exchange_rate";
    private static final String EXCHANGE_RATE_TO_DOLLAR = "exchange_rate_to_dollar";
    public static final String BANK_EXCHANGE_RATE = "bank_exchange_rate";
    public static final String BANK_EXCHANGE_RATE_INVERTED = "bank_exchange_rate_inverted";
    public static final String BANK_EXCHANGE_RATE_PERCENTAGE = "bank_exchange_rate_percentage";
    public static final String PAYPAL_PERCENTAGE = "paypal_percentage";
    public static final String AFIP_PERCENTAGE = "afip_percentage";
    public static final String SAVINGS_PERCENTAGE = "savings_percentage";
    public static final String AGENCY_EXCHANGE_RATE = "agency_exchange_rate";
    public static final String AGENCY_EXCHANGE_RATE_INVERTED = "agency_exchange_rate_inverted";
    public static final String LAST_UPDATE_DATE = "last_update_date_ms";
    public static final String ARE_UPDATES_ENABLED = "update_bank_exchange_rates";
    public static final String UPDATE_FREQUENCY = "update_frequency";
    public static final String USE_INTERNET_BANK_EXCHANGE_RATE = "use_internet_bank_exchange_rate";
    public static final String CURRENT_CURRENCY = "source_currency";
    public static final String CHOSEN_CURRENCIES = "chosen_currencies";
    public static final String CURRENCIES_SEPARATOR = ",";
    public static final String BLUE_DOLLAR_ARS = "blue_dollar_ars";

    public static final double DEFAULT_PAYPAL_PERCENTAGE = 7.5;
    public static final double DEFAULT_AFIP_PERCENTAGE = 35;
    public static final double DEFAULT_SAVINGS_PERCENTAGE = 20;
    public static final double DEFAULT_AGENCY_EXCHANGE_RATE = 0;
    public static final double DEFAULT_BANK_EXCHANGE_RATE = 0;
    public static final double DEFAULT_BANK_EXCHANGE_RATE_PERCENTAGE = 0;
    public static final double DEFAULT_INTERNET_EXCHANGE_RATE = 0;
    public static final int DEFAULT_UPDATE_FREQUENCY = 4;

    public static final String LAST_CONVERSION_TEXT = "last_conversion_text";
    public static final String LAST_CONVERSION_VALUE = "last_conversion_value";
    public static final String REMEMBER_LAST_CONVERSION = "remember_last_conversion";

    public static final String CURRENT_PREFS_VERSION = "current_prefs_version";
    public static final String SHOW_DISCOUNT = "show_discount";
    public static final String SHOW_TAXES = "show_taxes";
    public static final String SHOW_PESOS = "show_pesos";
    public static final String SHOW_CREDIT_CARD = "show_credit_card";
    public static final String SHOW_SAVINGS = "show_savings";
    public static final String SHOW_BLUE = "show_blue";
    public static final String SHOW_EXCHANGE_AGENCY = "show_exchange_agency";
    public static final String SHOW_PAYPAL = "show_paypal";
    private static final String DEFAULT_CURRENCY = CurrencyManager.USD;
    private static final String IS_NAV_DRAWER_NEW = "is_nav_drawer_new";

    private Map<Currency, SharedPreferences> preferencesByCurrency = new HashMap<Currency, SharedPreferences>();
    private SharedPreferences preferencesByApp;
    private Currency currentCurrency;

    public PreferencesManager() {
        //migratePreferencesFromVersion3();
        updatePreferencesToVersion7();
        updatePreferencesToVersion11();
        updatePreferencesToVersion14();

        updateCurrentPreferencesVersion();

        refreshCurrentCurrency();

        // if the user doesn't want to remember the conversions, we must forget the discount and taxes.
        // otherwise they will appear automatically
        if (!isRememberLastConversion()) {
            setDiscount(0);
            setTaxes(0);
            setLastConversionValue(0);
            AmountTextWatcher.lastOneChanged = null;
        }
    }

    public static PreferencesManager getInstance() {
        return instance;
    }

    private Context getAppContext() {
        return MyApplication.Companion.applicationContext();
    }

    protected SharedPreferences getPreferencesByApp() {
        if (preferencesByApp == null) {
            preferencesByApp = getAppContext().getSharedPreferences(PREFS_NAME_SHARED, 0);

            if (preferencesByApp == null)
                throw new IllegalStateException("Unable to get Shared preferences for the application");
        }

        return preferencesByApp;
    }

    private Map<Currency, SharedPreferences> getPreferencesByCurrency() {
        if (preferencesByCurrency.isEmpty()) {
            for (Currency curr : CurrencyManager.getInstance().getAllCurrencies()) {
                preferencesByCurrency.put(curr, MyApplication.Companion.applicationContext()
                        .getSharedPreferences(PREFS_NAME_CURRENCY + curr.getCode(), 0));
            }
            if (preferencesByCurrency.isEmpty())
                throw new IllegalStateException("Unable to load preferences by currency");
        }

        return preferencesByCurrency;
    }

    protected SharedPreferences getPreferencesForCurrentCurrency() {
        SharedPreferences result = getPreferencesByCurrency(getCurrentCurrency());
        if (result == null)
            throw new IllegalStateException("Unable to get preferences by currency for: " + getCurrentCurrency());
        return result;
    }

    public Set<String> getAllPreferenceKeys() {
        Set<String> result = new HashSet<>();
        result.add(INTERNET_EXCHANGE_RATE);
        result.add(BANK_EXCHANGE_RATE);
        result.add(BANK_EXCHANGE_RATE_INVERTED);
        result.add(BANK_EXCHANGE_RATE_PERCENTAGE);
        result.add(PAYPAL_PERCENTAGE);
        result.add(AFIP_PERCENTAGE);
        result.add(SAVINGS_PERCENTAGE);
        result.add(AGENCY_EXCHANGE_RATE);
        result.add(AGENCY_EXCHANGE_RATE_INVERTED);
        result.add(LAST_UPDATE_DATE);
        result.add(ARE_UPDATES_ENABLED);
        result.add(UPDATE_FREQUENCY);
        result.add(USE_INTERNET_BANK_EXCHANGE_RATE);
        result.add(CURRENT_CURRENCY);
        return result;
    }

    public void refreshCurrentCurrency() {
        String currCode = getPreferencesByApp().getString(CURRENT_CURRENCY, DEFAULT_CURRENCY);
        currentCurrency = CurrencyManager.getInstance().findCurrency(currCode);
    }

    public Currency getCurrentCurrency() {
        if (currentCurrency == null) {
            refreshCurrentCurrency();
        }
        return currentCurrency;
    }

    public void setCurrentCurrency(Currency curr) {
        if (!getCurrentCurrency().equals(curr)) {
            currentCurrency = curr;
            Editor editor = getPreferencesByApp().edit();
            editor.putString(CURRENT_CURRENCY, curr.getCode());
            editor.apply();
        }
    }

    public List<Currency> getChosenCurrencies() {
        List<Currency> result = new ArrayList<>();
        String chosenCurrencies = getPreferencesByApp().getString(CHOSEN_CURRENCIES, null);

        if (chosenCurrencies == null) {
            Currency curr = CurrencyManager.getInstance().findCurrency(DEFAULT_CURRENCY);
            if (curr != null)
                result.add(curr);
        } else {
            for (String code : chosenCurrencies.split(CURRENCIES_SEPARATOR)) {
                Currency curr = CurrencyManager.getInstance().findCurrency(code);
                if (curr != null)
                    result.add(curr);
            }
        }
        return result;
    }

    public void setChosenCurrencies(List<Currency> currencies) {
        String value = "";
        for (Currency curr : currencies) {
            if (value.length() != 0)
                value += CURRENCIES_SEPARATOR;

            value += curr.getCode();
        }

        Editor editor = getPreferencesByApp().edit();
        editor.putString(CHOSEN_CURRENCIES, value);
        editor.apply();
    }


    // TODO do this for all numeric preferences, because the value entered by the user could be "."
    public double getAfipPercentage() {
        String resultStr = getPreferencesByApp().getString(AFIP_PERCENTAGE, String.valueOf(DEFAULT_AFIP_PERCENTAGE));
        double result = DEFAULT_AFIP_PERCENTAGE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesByApp().edit();
            editor.putString(AFIP_PERCENTAGE, String.valueOf(DEFAULT_AFIP_PERCENTAGE));
            editor.apply();
        }
        return result;
    }

    public double getSavingsPercentage() {
        String resultStr = getPreferencesByApp().getString(SAVINGS_PERCENTAGE, String.valueOf(DEFAULT_SAVINGS_PERCENTAGE));
        double result = DEFAULT_SAVINGS_PERCENTAGE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesByApp().edit();
            editor.putString(SAVINGS_PERCENTAGE, String.valueOf(DEFAULT_SAVINGS_PERCENTAGE));
            editor.apply();
        }
        return result;
    }

    public boolean isAutomaticUpdateEnabled() {
        return getPreferencesByApp().getBoolean(ARE_UPDATES_ENABLED, true);
    }

    public int getUpdateFrequencyInHours() {
        int intResult = DEFAULT_UPDATE_FREQUENCY;
        String stringVal = getPreferencesByApp().getString(UPDATE_FREQUENCY, String.valueOf(DEFAULT_UPDATE_FREQUENCY));
        try {
            intResult = Integer.parseInt(stringVal);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesByApp().edit();
            editor.putString(UPDATE_FREQUENCY, String.valueOf(DEFAULT_UPDATE_FREQUENCY));
            editor.apply();
        }
        return intResult;
    }

    private SharedPreferences getPreferencesByCurrency(Currency curr) {
        return getPreferencesByCurrency().get(curr);
    }

    public Date getLastUpdateDate(Currency curr) {
        return new Date(getPreferencesByCurrency(curr).getLong(LAST_UPDATE_DATE, 0));
    }

    public void setLastUpdateDate(Date date, Currency curr) {
        getPreferencesByCurrency(curr).edit()
                .putLong(LAST_UPDATE_DATE, date.getTime())
                .apply();
    }

    public boolean isRememberLastConversion() {
        return getPreferencesByApp().getBoolean(REMEMBER_LAST_CONVERSION, true);
    }

    public EditorType getLastConversionType() {
        EditorType result = null;
        String typeName = getPreferencesByApp().getString(LAST_CONVERSION_TEXT, null);
        try {
            result = EditorType.valueOf(EditorType.class, typeName);
        } catch (Exception e) {
        }

        return result;
    }

    public void setLastConversionType(EditorType type) {
        Editor editor = getPreferencesByApp().edit();
        editor.putString(LAST_CONVERSION_TEXT, type.name());
        editor.apply();
    }

    public double getLastConversionValue() {
        double result = 0;
        String resultStr = getPreferencesByApp().getString(LAST_CONVERSION_VALUE, "0");

        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // this catch is for values stored with version 4 (1.3) of the program
        }
        return result;
    }

    public void setLastConversionValue(double value) {
        Editor editor = getPreferencesByApp().edit();
        editor.putString(LAST_CONVERSION_VALUE, Double.toString(value));
        editor.apply();
    }

    public double getDiscount() {
        return (double) getPreferencesByApp().getFloat(DISCOUNT, 0);
    }

    public void setDiscount(double discount) {
        Editor editor = getPreferencesByApp().edit();
        editor.putFloat(DISCOUNT, (float) discount);
        editor.apply();
    }

    public double getTaxes() {
        return (double) getPreferencesByApp().getFloat(TAXES, 0);
    }

    public void setTaxes(double taxes) {
        Editor editor = getPreferencesByApp().edit();
        editor.putFloat(TAXES, (float) taxes);
        editor.apply();
    }

    public double getBlueDollarToARSRate() {
        return (double) getPreferencesByApp().getFloat(BLUE_DOLLAR_ARS, 0);
    }

    public void setBlueDollarToArsRate(double blueRate) {
        Editor editor = getPreferencesByApp().edit();
        editor.putFloat(BLUE_DOLLAR_ARS, (float) blueRate);
        editor.apply();
    }


    public double getPayPalPercentage() {
        String resultStr = getPreferencesForCurrentCurrency().getString(PAYPAL_PERCENTAGE, String.valueOf(DEFAULT_PAYPAL_PERCENTAGE));
        double result = DEFAULT_PAYPAL_PERCENTAGE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(PAYPAL_PERCENTAGE, String.valueOf(DEFAULT_PAYPAL_PERCENTAGE));
            editor.apply();
        }
        return result;
    }

    public double getAgencyExchangeRate() {
        String resultStr = getPreferencesForCurrentCurrency().getString(AGENCY_EXCHANGE_RATE, String.valueOf(DEFAULT_AGENCY_EXCHANGE_RATE));
        double result = DEFAULT_AGENCY_EXCHANGE_RATE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(AGENCY_EXCHANGE_RATE, String.valueOf(DEFAULT_AGENCY_EXCHANGE_RATE));
            editor.apply();
        }
        return result;
    }

    public boolean isAgencyExchangeRateInverted() {
        return getPreferencesForCurrentCurrency().getBoolean(AGENCY_EXCHANGE_RATE_INVERTED, false);
    }

    public boolean isUseInternetBankExchangeRateEnabled() {
        return getPreferencesForCurrentCurrency().getBoolean(USE_INTERNET_BANK_EXCHANGE_RATE, true);
    }

    public double getBankExchangeRate() {
        String resultStr = getPreferencesForCurrentCurrency().getString(BANK_EXCHANGE_RATE, String.valueOf(DEFAULT_BANK_EXCHANGE_RATE));
        double result = DEFAULT_BANK_EXCHANGE_RATE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(BANK_EXCHANGE_RATE, String.valueOf(DEFAULT_BANK_EXCHANGE_RATE));
            editor.apply();
        }
        return result;
    }

    public void setBankExchangeRate(double value) {
        Editor editor = getPreferencesForCurrentCurrency().edit();
        editor.putString(BANK_EXCHANGE_RATE, Double.toString(value));
        editor.apply();
    }

    public boolean isBankExchangeRateInverted() {
        return getPreferencesForCurrentCurrency().getBoolean(BANK_EXCHANGE_RATE_INVERTED, false);
    }

    public double getBankCorrectionPercentage() {
        String resultStr = getPreferencesForCurrentCurrency().getString(BANK_EXCHANGE_RATE_PERCENTAGE, String.valueOf(DEFAULT_BANK_EXCHANGE_RATE_PERCENTAGE));
        double result = DEFAULT_BANK_EXCHANGE_RATE_PERCENTAGE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(BANK_EXCHANGE_RATE_PERCENTAGE, String.valueOf(DEFAULT_BANK_EXCHANGE_RATE_PERCENTAGE));
            editor.apply();
        }
        return result;
    }

    public double getInternetExchangeRate() {
        String resultStr = getPreferencesForCurrentCurrency().getString(INTERNET_EXCHANGE_RATE, String.valueOf(DEFAULT_INTERNET_EXCHANGE_RATE));
        double result = DEFAULT_INTERNET_EXCHANGE_RATE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(INTERNET_EXCHANGE_RATE, String.valueOf(DEFAULT_INTERNET_EXCHANGE_RATE));
            editor.apply();
        }
        return result;
    }

    public void setInternetExchangeRate(Currency curr, double value) {
        Editor editor = getPreferencesByCurrency(curr).edit();
        editor.putString(INTERNET_EXCHANGE_RATE, Double.toString(value));
        editor.apply();
    }

    public double getExchangeRateToDollar() {
        String resultStr = getPreferencesForCurrentCurrency().getString(EXCHANGE_RATE_TO_DOLLAR, String.valueOf(DEFAULT_INTERNET_EXCHANGE_RATE));
        double result = DEFAULT_INTERNET_EXCHANGE_RATE;
        try {
            result = Double.parseDouble(resultStr);
        } catch (NumberFormatException e) {
            // update the value so it won't throw an exception next time
            Editor editor = getPreferencesForCurrentCurrency().edit();
            editor.putString(EXCHANGE_RATE_TO_DOLLAR, String.valueOf(DEFAULT_INTERNET_EXCHANGE_RATE));
            editor.apply();
        }
        return result;
    }

    public void setExchangeRateToDollar(Currency curr, double value) {
        Editor editor = getPreferencesByCurrency(curr).edit();
        editor.putString(EXCHANGE_RATE_TO_DOLLAR, Double.toString(value));
        editor.apply();
    }

    public void updateAllBankExchangeRatesWhichAreUsingInternetRates() {
        for (Currency curr : getChosenCurrencies()) {
            // if "Use Internet Bank Exchange Rate" is enabled for this currency, update its "Bank Exchange Rate" value using the one from Internet
            boolean isUseInternetBankExchangeRateEnabled = getPreferencesByCurrency(curr).getBoolean(USE_INTERNET_BANK_EXCHANGE_RATE, true);
            if (isUseInternetBankExchangeRateEnabled) {
                String internetValue = getPreferencesByCurrency(curr).getString(INTERNET_EXCHANGE_RATE, String.valueOf(DEFAULT_INTERNET_EXCHANGE_RATE));
                Editor editor = getPreferencesByCurrency(curr).edit();
                editor.putString(BANK_EXCHANGE_RATE, internetValue);
                editor.apply();
            }
        }
    }

    public boolean isShowDiscount() {
        return getPreferencesByApp().getBoolean(SHOW_DISCOUNT, true);
    }

    public boolean isShowTaxes() {
        return getPreferencesByApp().getBoolean(SHOW_TAXES, true);
    }

    public boolean isShowPesos() {
        return getPreferencesByApp().getBoolean(SHOW_PESOS, true);
    }

    public boolean isShowCreditCard() {
        return getPreferencesByApp().getBoolean(SHOW_CREDIT_CARD, true);
    }

    public boolean isShowSavings() {
        return getPreferencesByApp().getBoolean(SHOW_SAVINGS, true);
    }

    public boolean isShowBlue() {
        return getPreferencesByApp().getBoolean(SHOW_BLUE, true);
    }

    public boolean isShowExchangeAgency() {
        return getPreferencesByApp().getBoolean(SHOW_EXCHANGE_AGENCY, true);
    }

    public boolean isShowPaypal() {
        return getPreferencesByApp().getBoolean(SHOW_PAYPAL, true);
    }

    private void log(String msg) {
        Log.i(PreferencesManager.class.getSimpleName(), msg);
    }


    private int getCurrentPreferencesVersion() {
        return getPreferencesByApp().getInt(CURRENT_PREFS_VERSION, 0);
    }

    private int getCurrentAppVersion() {
        try {
            return getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean isCurrentPreferencesVersionUpToDate() {
        return getCurrentPreferencesVersion() == getCurrentAppVersion();
    }

    public void updateCurrentPreferencesVersion() {
        if (!isCurrentPreferencesVersionUpToDate()) {
            int newVersion = getCurrentAppVersion();
            Editor editor = getPreferencesByApp().edit();
            editor.putInt(CURRENT_PREFS_VERSION, newVersion);
            editor.apply();
        }
    }

    /**
     * Used to open the nav drawer automatically when it's a new feature
     */
    public boolean isNavDrawerANewFeature() {
        return getPreferencesByApp().getBoolean(IS_NAV_DRAWER_NEW, true);
    }

    public void setIsNavDrawerNew(boolean isNew) {
        Editor editor = getPreferencesByApp().edit();
        editor.putBoolean(IS_NAV_DRAWER_NEW, isNew);
        editor.apply();
    }

    /**
     * Version 3 used the default file for shared preferences.
     * We want to migrate the settings from that file into the new preferences file.
     */
    public void migratePreferencesFromVersion3() {
        try {
            File sharedPrefsDir = new File(getAppContext().getFilesDir(), "../shared_prefs");
            String oldPrefsFile = getAppContext().getApplicationContext().getPackageName() + "_preferences";
            File oldPrefsFilePath = new File(sharedPrefsDir, oldPrefsFile + ".xml");
            File oldInternetCurrenciesFile = new File(sharedPrefsDir, "MyPrefsFile.xml");

            if (oldPrefsFilePath.exists())
                log("Old preferences file found");
            else
                log("There are no old preferences to migrate");

            boolean hasNewPreferences = getPreferencesByApp().getString(CURRENT_CURRENCY, null) != null;

            if (hasNewPreferences)
                log("New preferences file found");
            else
                log("New preferences file NOT found");

            if (oldPrefsFilePath.exists() && !hasNewPreferences) {
                final String updateBankExchangeRate = "update_bank_exchange_rate";
                //final String useLightIcons              = "use_light_icons";
                final String payPalPercentage = "paypal_percentage";
                final String sourceCurrency = "source_currency";
                final String afipPercentage = "afip_percentage";

                final String bankExchangeRate = "bank_exchange_rate";
                final String bankExchangeRatePercentage = "bank_exchange_rate_percentage";
                final String bankExchangeRateInverted = "bank_exchange_rate_inverted";

                final String agencyExchangeRate = "agency_exchange_rate";
                final String agencyExchangeRateInverted = "agency_exchange_rate_inverted";

                SharedPreferences oldPrefs = getAppContext().getSharedPreferences(oldPrefsFile, 0);

                log("Migrating preferences...");

                String currencyStr = oldPrefs.getString(sourceCurrency, null);

                if (currencyStr != null) {
                    log("Migrating currency: " + currencyStr);
                    Currency currency = CurrencyManager.getInstance().findCurrency(currencyStr);
                    //Currencies currency = Currencies.valueOf(currencyStr);
                    setCurrentCurrency(currency);
                    Editor editorCurr = getPreferencesByCurrency(currency).edit();
                    Editor editorApp = getPreferencesByApp().edit();

                    boolean update = oldPrefs.getBoolean(updateBankExchangeRate, true);
                    editorApp.putBoolean(ARE_UPDATES_ENABLED, update);
                    editorCurr.putBoolean(USE_INTERNET_BANK_EXCHANGE_RATE, update);
                    log("Updates enabled? " + update);
					
					/*boolean lightIcons = oldPrefs.getBoolean(useLightIcons, true);
					editorApp.putBoolean(USE_LIGHT_ICONS, lightIcons);
					log("Use light icons? " + lightIcons);*/

                    String bankRate = oldPrefs.getString(bankExchangeRate, "0");
                    editorCurr.putString(BANK_EXCHANGE_RATE, bankRate);
                    log("Bank exchange rate: " + bankRate);

                    String bankRatePerc = oldPrefs.getString(bankExchangeRatePercentage, "0");
                    editorCurr.putString(BANK_EXCHANGE_RATE_PERCENTAGE, bankRatePerc);
                    log("Bank exchange rate percentage: " + bankRatePerc);

                    boolean bankInvert = oldPrefs.getBoolean(bankExchangeRateInverted, false);
                    editorCurr.putBoolean(BANK_EXCHANGE_RATE_INVERTED, bankInvert);
                    log("Invert bank conversion? " + bankInvert);

                    String agencyRate = oldPrefs.getString(agencyExchangeRate, "0");
                    editorCurr.putString(AGENCY_EXCHANGE_RATE, agencyRate);
                    log("Agency exchange rate: " + agencyRate);

                    boolean agencyRateInv = oldPrefs.getBoolean(agencyExchangeRateInverted, false);
                    editorCurr.putBoolean(AGENCY_EXCHANGE_RATE_INVERTED, agencyRateInv);
                    log("Invert agency conversion? " + agencyRateInv);

                    String payPal = oldPrefs.getString(payPalPercentage, "0");
                    editorCurr.putString(PAYPAL_PERCENTAGE, payPal);
                    log("PayPal exchange rate: " + payPal);

                    String afip = oldPrefs.getString(afipPercentage, "0");
                    editorApp.putString(AFIP_PERCENTAGE, afip);
                    log("AFIP percentage: " + afip);

                    editorCurr.apply();
                    editorApp.apply();
                    log("Finished migrating preferences!");

                    // delete old files
                    boolean deletedOldPrefs = oldPrefsFilePath.delete();
                    if (deletedOldPrefs)
                        log("Deleted old preferences file");
                    else
                        log("Unable to delete old preferences file");

                    if (oldInternetCurrenciesFile.exists()) {
                        boolean result = oldInternetCurrenciesFile.delete();
                        if (result)
                            log("Deleted old internet currencies file");
                        else
                            log("Unable to delete old internet currencies file");
                    }
                }
            } else {
                log("Skipping preferences migration from an old version. It's not necesary.");
            }
        } catch (Exception e) {
            log("There was an error migrating the old preferences to the new format.");
            log(e.toString());
        }
    }

    /**
     * Updates the % for AFIP to 20%
     */
    public void updatePreferencesToVersion7() {
        try {
            if (getCurrentPreferencesVersion() < 7) {
                // update % for AFIP
                double currentAFIP = getAfipPercentage();
                if (currentAFIP > 14 && currentAFIP < 16) {
                    Editor editor = getPreferencesByApp().edit();
                    editor.putString(AFIP_PERCENTAGE, "20");
                    editor.apply();
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * Updates the list of favorite currencies to have what used to be all the currencies
     */
    public void updatePreferencesToVersion11() {
        try {
            if (getCurrentPreferencesVersion() < 11) {
                // update the list of favorite preferences: BRL, USD, EUR, CLP, UYU, MXN, GBP
                List<Currency> allPreviousCurrencies = new ArrayList<>();
                for (String str : new String[]{"USD", "EUR", "GBP", "CLP", "MXN", "UYU", "BRL"}) {
                    Currency curr = CurrencyManager.getInstance().findCurrency(str);
                    if (curr != null)
                        allPreviousCurrencies.add(curr);
                }
                setChosenCurrencies(allPreviousCurrencies);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Updates the % for AFIP to 35% (as of 03 December 2013)
     */
    public void updatePreferencesToVersion14() {
        try {
            if (getCurrentPreferencesVersion() < 14) {
                // update % for AFIP
                double currentAFIP = getAfipPercentage();
                double oldAFIP = 20;

                if (currentAFIP > oldAFIP - 1 && currentAFIP < oldAFIP + 1) {
                    Editor editor = getPreferencesByApp().edit();
                    editor.putString(AFIP_PERCENTAGE, String.valueOf(DEFAULT_AFIP_PERCENTAGE));
                    editor.apply();
                }
            }

        } catch (Exception e) {
        }
    }
}
