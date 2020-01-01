package com.smartpocket.cuantoteroban;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class SearchCurrenciesActivity extends ListActivity {
	private enum COLUMN_NAMES {FLAG, CURRENCY_NAME}

    @Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      searchAllUnusedCurrencies(query);
	    }
	}

	private void searchAllUnusedCurrencies(String query) {
		
		String[] from = new String[] {COLUMN_NAMES.FLAG.name(), COLUMN_NAMES.CURRENCY_NAME.name()};
        int[] to = new int[] { R.id.addCurrencyFlag, R.id.addCurrencyName};
 
        Set<Currency> currencies = CurrencyManager.getInstance().getAllCurrencies();
        
        // prepare the list of all records
        List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
        for(Currency curr : currencies){
        	if (curr.getName().toLowerCase().contains(query.toLowerCase())){
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put(COLUMN_NAMES.FLAG.name(), curr.getFlagIdentifier());
	            map.put(COLUMN_NAMES.CURRENCY_NAME.name(), curr.getName());
	            fillMaps.add(map);
        	}
        }
 
        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.add_currency_row, from, to);
        
		
		
		//ListAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
		
		
		
		setListAdapter(adapter);
		
	}
	
	
	

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_currencies, menu);
		return true;
	}
*/
}
