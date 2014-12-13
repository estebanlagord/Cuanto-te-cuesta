package com.smartpocket.cuantoteroban;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AddCurrency extends ActionBarActivity {
	private static enum COLUMN_NAMES {FLAG, NAME, CODE};
	private SearchView searchView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_currency);
		Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
		
		ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
	    handleIntent(getIntent());
		
	    ListView listView = (ListView)findViewById(R.id.unused_currencies_list);
	    listView.setFastScrollEnabled(true);

	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);
				String currCode = (String)map.get(COLUMN_NAMES.CODE.name());

				Currency newCurrency = CurrencyManager.getInstance().findCurrency(currCode);
				CurrencyManager.getInstance().addToUserCurrencies(newCurrency);
				
				MainActivity.getInstance().updateExchangeRatesAfterAddingNewCurrency();
				
				Intent resultIntent = new Intent();
				//Bundle bundle = new Bundle();
				//bundle.putString(Currency.CODE, currCode);
				//resultIntent.putExtras(bundle);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String searchText = intent.getStringExtra(SearchManager.QUERY);
	    	
	    	Log.d("Add currency", "Searched for: " + searchText);
	    	updateCurrenciesList(searchText);
	    } else {
	    	updateCurrenciesList(null);
	    }
	}

	
	private void updateCurrenciesList(String query) {
		if (query!= null)
			query = query.toLowerCase(Locale.US);
		
		String[] from = new String[] {COLUMN_NAMES.FLAG.name(), COLUMN_NAMES.NAME.name(), COLUMN_NAMES.CODE.name() };
        int[] to = new int[] { R.id.addCurrencyFlag, R.id.addCurrencyName, R.id.addCurrencyCode};
 
        Set<Currency> currencies = CurrencyManager.getInstance().getAllUnusedCurrencies();
        
        // prepare the list of all records
        List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
        for(Currency curr : currencies){
        	if (curr.matchesQuery(query))
        	{
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put(COLUMN_NAMES.FLAG.name(), curr.getFlagIdentifier());
	            map.put(COLUMN_NAMES.NAME.name(), curr.getName());
	            map.put(COLUMN_NAMES.CODE.name(), curr.getCode());
	            fillMaps.add(map);
        	}
        }
 
        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.add_currency_row, from, to);
        
		ListView listView = (ListView)findViewById(R.id.unused_currencies_list);
		listView.setAdapter(adapter);
		
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_currency, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		
		// the Search View only works on Android 2.1 (API 8)
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
	    	menu.removeItem(searchItem.getItemId());
	    } else {
		    searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		    
		    searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
		    	
				@Override
				public boolean onSuggestionSelect(int position) {
					return true;
				}
				
				@Override
				public boolean onSuggestionClick(int position) {
			           CursorAdapter selectedView = searchView.getSuggestionsAdapter();
			           Cursor cursor = (Cursor) selectedView.getItem(position);
			           int index = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
			           searchView.setQuery(cursor.getString(index), true);
			           return true;
				}
			});
		    
		    MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
				
				@Override
				public boolean onMenuItemActionCollapse(MenuItem arg0) {
					updateCurrenciesList(null);
					return true;
				}

				@Override
				public boolean onMenuItemActionExpand(MenuItem arg0) {
					return true;
				}
			});

		    
		    
		    // Configure the search info and add any event listeners
		    
		    
		    // Get the SearchView and set the searchable configuration
		    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		    //SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

		    //SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		    // Assumes current activity is the searchable activity
		    //SearchableInfo searchInfo =  searchManager.getSearchablesInGlobalSearch().get(0);
		    
		    
		    //SearchCurrenciesActivity searchActivity = new SearchCurrenciesActivity();
//		    ComponentName component = new ComponentName(SearchCurrenciesActivity.class.getPackage().getName(),
//		    		SearchCurrenciesActivity.class.getCanonicalName());
		    
		    //searchView.setSearchableInfo(searchInfo);
//		    SearchableInfo searchableInfo = searchManager.getSearchableInfo(component);
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
		    searchView.setSearchableInfo(searchableInfo);
		    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

	    }


	    
	    
		return true;
	}



}
