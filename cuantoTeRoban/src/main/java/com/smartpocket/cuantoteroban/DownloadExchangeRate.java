package com.smartpocket.cuantoteroban;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DownloadExchangeRate extends	AsyncTask<String, Integer, String> {
	
	public static boolean updateInProgress = false;
	
	//private static final String EXCHANGE_RATE_URL = "http://download.finance.yahoo.com/d/quotes.csv?s=BRLARS=x,USDARS=x,EURARS=x,CLPARS=x,UYUARS=x,MXNARS=x,GBPARS=x&f=l1";
	private static final String EXCHANGE_RATE_URL_PRE = "http://download.finance.yahoo.com/d/quotes.csv?s=";
	private static final String EXCHANGE_RATE_URL_POS = "&f=l1";
    private static final String BLUE_DOLLAR_URL = "http://www.ambito.com/economia/mercados/monedas/dolar/info/?ric=ARSB=";
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat(    "dd/MMM HH:mm", new Locale("es", "AR"));
	private final SimpleDateFormat prefDateFormat    = new SimpleDateFormat("MM/dd/yyyy HH:mm", new Locale("es", "AR"));
	private final MainActivity mainActivity;
	private final boolean force;
	private final TextView lastUpdateStr;
	//private final ProgressBar progressCircle;
	private enum Results { SUCCESS, NO_INTERNET, PARSE_ERROR, CONNECTION_ERROR,  PARSE_ERROR_BLUE, CONNECTION_ERROR_BLUE }


    DownloadExchangeRate(MainActivity mainActivity, boolean force) {
		this.mainActivity = mainActivity;
		this.force = force;
		lastUpdateStr = this.mainActivity.findViewById(R.id.textLastUpdateValue);
	}

	@Override
	protected synchronized String doInBackground(String... sUrl) {
		String result = null;
		
		if(!isNetworkAvailable())
			return Results.NO_INTERNET.name();

        result = loadExchangeRates();
        boolean wasLoadExchangeRatesSuccessful = Results.valueOf(result) == Results.SUCCESS;
        boolean isShowingBlueConversion = PreferencesManager.getInstance().isShowBlue();

        if (wasLoadExchangeRatesSuccessful && isShowingBlueConversion)
            result = loadBlueDollarRate();

        return result;
	}

    private String loadExchangeRates() {
        InputStream input = null;
        String result;
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

                // process USDXXX conversions

                Double newExchangeRateToDollar = Double.parseDouble(reader.readLine());
                PreferencesManager.getInstance().setExchangeRateToDollar(curr, newExchangeRateToDollar);
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

    private String loadBlueDollarRate() {
        InputStream input = null;
        String result;
        try {
            URL url = new URL(BLUE_DOLLAR_URL);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            input = new BufferedInputStream(url.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            final String token = "id=\"venta\"".toLowerCase();

            String line = reader.readLine();
            while (line != null) {

                if (line.toLowerCase().contains(token)) {
                    Pattern p = Pattern.compile("[0-9,]+");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        String numberStr = m.group();
                        numberStr = numberStr.replaceAll(",", "."); // replace , for .
                        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
                        double blueRate = nf.parse(numberStr).doubleValue();
                        Log.i("DownloadExchangeRate", "Found blue: " + blueRate);
                        PreferencesManager.getInstance().setBlueDollarToArsRate(blueRate);
                        break;
                    }
                }
                line = reader.readLine();
            }
            result = Results.SUCCESS.name();

        } catch (NumberFormatException e) {
            result = Results.PARSE_ERROR_BLUE.name();
        } catch (NullPointerException e) {
            result = Results.PARSE_ERROR_BLUE.name();
        } catch (Exception e){
            result = Results.CONNECTION_ERROR_BLUE.name();
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
    	String dateStr;
    	
    	switch (Results.valueOf(result)) {
        case NO_INTERNET:
            if (force)
                Utilities.showToast("No hay conexión a Internet");
            break;
		case PARSE_ERROR:
			Utilities.showToast("Error al leer las cotizaciones de Internet");
			break;
		case CONNECTION_ERROR:
			Utilities.showToast("No se pudieron actualizar las cotizaciones desde Internet");
			break;
        case PARSE_ERROR_BLUE:
            Utilities.showToast("Error al leer la cotización del dólar blue de Internet");
            break;
        case CONNECTION_ERROR_BLUE:
            Utilities.showToast("No se pudo actualizar la cotización del dólar blue desde Internet");
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

                return lastUpdateCal.before(now);
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
            String code = curr.getCode().toUpperCase(Locale.US);
            result.append(code + "ARS=x," + code + "USD=x");
			if (i < currencies.size() - 1)
				result.append(',');
		}
		
		result.append(EXCHANGE_RATE_URL_POS);
		return result.toString();
	}
}
