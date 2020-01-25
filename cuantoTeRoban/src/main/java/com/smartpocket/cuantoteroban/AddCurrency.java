package com.smartpocket.cuantoteroban;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AddCurrency extends Fragment {
    private enum COLUMN_NAMES {FLAG, NAME, CODE}

    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_currency, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_awesome_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

//	    handleIntent(getIntent());

        listView = view.findViewById(R.id.unused_currencies_list);
        listView.setFastScrollEnabled(true);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            closeKeyboard();
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);
            String currCode = (String) map.get(COLUMN_NAMES.CODE.name());

            Currency newCurrency = CurrencyManager.getInstance().findCurrency(currCode);
            CurrencyManager.getInstance().addToUserCurrencies(newCurrency);

//            Intent resultIntent = new Intent();
//				setResult(RESULT_OK, resultIntent);
//				finish();
            NavHostFragment.findNavController(AddCurrency.this).navigateUp();
        });

        setHasOptionsMenu(true);
        updateCurrenciesList(null);
    }


    private void updateCurrenciesList(String query) {
        if (query != null)
            query = query.toLowerCase(Locale.US);

        String[] from = new String[]{COLUMN_NAMES.FLAG.name(), COLUMN_NAMES.NAME.name(), COLUMN_NAMES.CODE.name()};
        int[] to = new int[]{R.id.addCurrencyFlag, R.id.addCurrencyName, R.id.addCurrencyCode};

        Set<Currency> currencies = CurrencyManager.getInstance().getAllUnusedCurrencies();

        // prepare the list of all records
        List<HashMap<String, Object>> fillMaps = new ArrayList<>();
        for (Currency curr : currencies) {
            if (curr.matchesQuery(query)) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(COLUMN_NAMES.FLAG.name(), curr.getFlagIdentifier());
                map.put(COLUMN_NAMES.NAME.name(), curr.getName());
                map.put(COLUMN_NAMES.CODE.name(), curr.getCode());
                fillMaps.add(map);
            }
        }

        // fill in the grid_item layout
        SimpleAdapter adapter = new AddCurrencyAdapter(requireContext(), fillMaps, R.layout.add_currency_row, from, to);

        ListView listView = requireView().findViewById(R.id.unused_currencies_list);
        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.add_currency, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (listView.getCount() == 1) {
                    listView.performItemClick(null, 0, 0);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateCurrenciesList(newText);
                return true;
            }
        });


        // Configure the search info and add any event listeners


        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        //SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        //SearchableInfo searchInfo =  searchManager.getSearchablesInGlobalSearch().get(0);


        //SearchCurrenciesActivity searchActivity = new SearchCurrenciesActivity();
//		    ComponentName component = new ComponentName(SearchCurrenciesActivity.class.getPackage().getName(),
//		    		SearchCurrenciesActivity.class.getCanonicalName());

        //searchView.setSearchableInfo(searchInfo);
//		    SearchableInfo searchableInfo = searchManager.getSearchableInfo(component);
//        SearchableInfo searchableInfo = searchManager.getSearchableInfo(requireActivity().getComponentName());
//        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

    }

    class AddCurrencyAdapter extends SimpleAdapter implements SectionIndexer {
        //Set<Character> sections = new TreeSet<Character>();
        Map<String, Integer> mapIndex = new TreeMap<>();
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
        AddCurrencyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);

            for (int x = 0; x < data.size(); x++) {
                //for (Map<String, ?> map : data) {
                Map<String, ?> map = data.get(x);
                String name = map.get(COLUMN_NAMES.NAME.name()).toString().toUpperCase();
                if (name.length() > 0) {
                    String ch = name.substring(0, 1);
                    if (!mapIndex.containsKey(ch))
                        mapIndex.put(ch, x);
                }
            }

            Set<String> sectionLetters = mapIndex.keySet();
            sections = sectionLetters.toArray(new String[0]);
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

    private void closeKeyboard() {
        // Check if no view has focus:
        View view = requireView().findFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
