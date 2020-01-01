package com.smartpocket.cuantoteroban;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AddCurrency extends AppCompatActivity {
	private enum COLUMN_NAMES {FLAG, NAME, CODE}

	private SearchView searchView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_currency);
		Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
		
		ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
	    handleIntent(getIntent());
		
	    ListView listView = findViewById(R.id.unused_currencies_list);
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
		super.onNewIntent(intent);
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
        SimpleAdapter adapter = new AddCurrencyAdapter(this, fillMaps, R.layout.add_currency_row, from, to);
        
		ListView listView = findViewById(R.id.unused_currencies_list);
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);

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

	class AddCurrencyAdapter extends SimpleAdapter implements SectionIndexer{
		//Set<Character> sections = new TreeSet<Character>();
		Map<String, Integer> mapIndex = new TreeMap<String, Integer>();
		String[] sections;

		/**
		 * Constructor
		 *
		 * @param context  The context where the View associated with this SimpleAdapter is running
		 * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
		 *                 Maps contain the data for each row, and should include all the entries specified in
		 *                 "from"
		 * @param resource Resource identifier of a view layout that defines the views for this list
		 *                 item. The layout file should include at least those named views defined in "to"
		 * @param from     A list of column names that will be added to the Map associated with each
		 *                 item.
		 * @param to       The views that should display column in the "from" parameter. These should all be
		 *                 TextViews. The first N views in this list are given the values of the first N columns
		 */
		public AddCurrencyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);

			for (int x = 0; x < data.size() ; x++) {
			//for (Map<String, ?> map : data) {
				Map<String, ?> map = data.get(x);
				String name = map.get(COLUMN_NAMES.NAME.name()).toString().toUpperCase();
				if (name != null && name.length() > 0) {
						String ch = name.substring(0, 1);
						if (!mapIndex.containsKey(ch))
							mapIndex.put(ch, x);
				}
			}

			Set<String> sectionLetters = mapIndex.keySet();
			sections = sectionLetters.toArray(new String[sectionLetters.size()]);
		}

		@Override
		public Object[] getSections() {
			return sections;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			return mapIndex.get(sections[sectionIndex]);
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}
	}

}
