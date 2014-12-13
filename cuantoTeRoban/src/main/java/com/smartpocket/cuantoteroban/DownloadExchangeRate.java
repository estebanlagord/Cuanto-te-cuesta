package com.smartpocket.cuantoteroban;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.TextView;

class DownloadExchangeRate extends	AsyncTask<String, Integer, String> {
	
	public static boolean updateInProgress = false;
	
	//private static final String EXCHANGE_RATE_URL = "http://download.finance.yahoo.com/d/quotes.csv?s=BRLARS=x,USDARS=x,EURARS=x,CLPARS=x,UYUARS=x,MXNARS=x,GBPARS=x&f=l1";
	private static final String EXCHANGE_RATE_URL_PRE = "http://download.finance.yahoo.com/d/quotes.csv?s=";
	private static final String EXCHANGE_RATE_URL_POS = "&f=l1";
	private final SimpleDateFormat displayDateFormat = new SimpleDateFormat(    "dd/MMM HH:mm", new Locale("es", "AR"));
	private final SimpleDateFormat prefDateFormat    = new SimpleDateFormat("MM/dd/yyyy HH:mm", new Locale("es", "AR"));
	private final MainActivity mainActivity;
	private final boolean force;
	private final TextView lastUpdateStr;
	//private final ProgressBar progressCircle;
	private enum Results { SUCCESS, PARSE_ERROR, CONNECTION_ERROR, NO_INTERNET };


	DownloadExchangeRate(MainActivity mainActivity, boolean force) {
		this.mainActivity = mainActivity;
		this.force = force;
		lastUpdateStr = (TextView)this.mainActivity.findViewById(R.id.textLastUpdateValue);
		//progressCircle = (ProgressBar)this.mainActivity.findViewById(R.id.progressBar);

	}

	@Override
	protected synchronized String doInBackground(String... sUrl) {
		InputStream input = null;
		String result = null;
		
		if(!isNetworkAvailable())
			return Results.NO_INTERNET.name();
		
		try {
			URL url = new URL(getExchangeRateURL());
			URLConnection connection = url.openConnection();
			connection.connect();

			// download the file
			input = new BufferedInputStream(url.openStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			//for (Currency curr : CurrencyManager.getInstance().getAllCurrencies()) {
			for (Currency curr : PreferencesManager.getInstance().getChosenCurrencies()) {
				Double newInternetExchangeRate = Double.parseDouble(reader.readLine());
				PreferencesManager.getInstance().setInternetExchangeRate(curr, newInternetExchangeRate);
				/*
				if (newInternetExchangeRate == 0)
					System.out.println("Moneda con problema: " + curr.getCode() + " " + curr.getName());
				else
					System.out.println("Procesando: " + curr.getCode() + " " + curr.getName());
				*/
			}
			
			result = Results.SUCCESS.name();

		} catch (NumberFormatException e) {
			result = Results.PARSE_ERROR.name();
		} catch (NullPointerException e) {
			result = Results.PARSE_ERROR.name();
		} catch (Exception e){
			result = Results.CONNECTION_ERROR.name();
		} finally{
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {}
		}
		
    	return result;
	}
	
    @Override
    protected synchronized void onPreExecute() {
        super.onPreExecute();
        
        updateInProgress = true;
        
        lastUpdateStr.setText(mainActivity.getResources().getString(R.string.updating));
        //progressCircle.setVisibility(View.VISIBLE);
        this.mainActivity.updateRefreshProgress();
    }

    
    @Override
    protected synchronized void onPostExecute(String result) {
    	super.onPostExecute(result);
    	String dateStr = PreferencesManager.getInstance().getLastUpdateDate();
    	
    	switch (Results.valueOf(result)) {
		case PARSE_ERROR:
			Utilities.showToast("Error al parsear las cotizaciones de Internet");
			break;
		case NO_INTERNET:
			if (force)
				Utilities.showToast("No hay conexión a Internet");
			break;
		case CONNECTION_ERROR:
			Utilities.showToast("No se pudieron actualizar las cotizaciones desde Internet");
			break;
		default:
			// SUCCESS
			Date date = new Date();
	        dateStr = prefDateFormat.format(date);
	
	    	// update preference's value
	        PreferencesManager.getInstance().setLastUpdateDate(dateStr);
	    	
	    	// if the user wants automatic updates, update the preference for "Bank exchange rate"
	        if (PreferencesManager.getInstance().isAutomaticUpdateEnabled() || this.force){
	    		PreferencesManager.getInstance().updateAllBankExchangeRatesWhichAreUsingInternetRates();
	    	}
	    	break;
		}
    	
    	loadLastUpdateStr();
        //progressCircle.setVisibility(View.GONE);
        
        this.mainActivity.recalculateConversionRates();
        
        updateInProgress = false;
        this.mainActivity.updateRefreshProgress();
    }

	private String convertPreferenceDateStrToDisplayDate(String prefDateStr){
		try {
			Date date = prefDateFormat.parse(prefDateStr);
			String result = displayDateFormat.format(date);
			return result;
		} catch (ParseException e) {
			return prefDateStr;
		}
	}

	/**
	 * @return true if the program hasn't updated exchange rates in a period of time longer than the one specified by the user
	 */
    public synchronized boolean needsUpdate(){
			try {
				Calendar aVeryLongTimeAgo = Calendar.getInstance();
				aVeryLongTimeAgo.set(1971, 0, 1, 0, 0, 0);

				String lastUpdateStr = PreferencesManager.getInstance().getLastUpdateDate();
				if (lastUpdateStr.equals(mainActivity.getResources().getString(R.string.LastUpdateNever)))
					lastUpdateStr = prefDateFormat.format(aVeryLongTimeAgo.getTime());
				
				Date lastUpdate = prefDateFormat.parse(lastUpdateStr);
				Calendar now = Calendar.getInstance();
				Calendar lastUpdateCal = Calendar.getInstance();

				// get the number of hours for "Update Frequency", as specified by the user
				int updateFrequency = PreferencesManager.getInstance().getUpdateFrequencyInHours();
				
				// add hours to the lastUpdate and check if its still before "now"
				lastUpdateCal.setTime(lastUpdate);
				lastUpdateCal.add(Calendar.HOUR_OF_DAY, updateFrequency);

				if (lastUpdateCal.before(now)){
					return true;
				} else {
					return false;
				}
			} catch (ParseException e) {
				return true;
			}

    }

	public synchronized void loadLastUpdateStr() {
		String lastUpdate = PreferencesManager.getInstance().getLastUpdateDate();
		lastUpdateStr.setText(convertPreferenceDateStrToDisplayDate(lastUpdate));
	}
	
	
	private synchronized boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager  = (ConnectivityManager) this.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}
	
	private String getExchangeRateURL() {
		StringBuffer result = new StringBuffer(EXCHANGE_RATE_URL_PRE);
		
		List<Currency> currencies = PreferencesManager.getInstance().getChosenCurrencies();
		//List<Currency> currencies = new ArrayList<Currency>();
		//currencies.addAll(CurrencyManager.getInstance().getAllCurrencies());
		//System.out.println("There are " + currencies.size() + " currencies");
		
		for (int i = 0; i< currencies.size() ; i++) {
			Currency curr = currencies.get(i);
			result.append(curr.getCode().toUpperCase(Locale.US) + "ARS=x");
			if (i < currencies.size() - 1)
				result.append(',');
		}
		
		result.append(EXCHANGE_RATE_URL_POS);
		return result.toString();
	}
}
