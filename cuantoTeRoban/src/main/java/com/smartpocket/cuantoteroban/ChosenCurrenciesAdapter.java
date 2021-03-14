package com.smartpocket.cuantoteroban;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

@ActivityScoped
public class ChosenCurrenciesAdapter extends BaseAdapter {
	private final Context context;
	private final PreferencesManager preferences;
	List<Currency> currencies;
	LayoutInflater inflater;
	/** Selected item position	*/
	private Currency mSelectedItem;

	@Inject
	public ChosenCurrenciesAdapter(@ActivityContext Context context, PreferencesManager preferences) {
		this.context = context;
		this.preferences = preferences;
		this.currencies = new ArrayList<>(preferences.getChosenCurrencies());
		this.inflater = ((Activity)context).getLayoutInflater();
		
		updateSelectedItem();
	}
	
	public Currency getSelectedItem() {
		return mSelectedItem;
	}
	
	public void setSelectedItem(Currency selectedItem) {
		mSelectedItem = selectedItem;
		notifyDataSetChanged();
	}
	
	public void updateSelectedItem() {
		Currency currentCurr = preferences.getCurrentCurrency();
		setSelectedItem(currentCurr);
	}
	
	@Override
	public int getCount() {
		return currencies.size();
	}

	@Override
	public Object getItem(int position) {
		return currencies.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Currency currency = (Currency)getItem(position);
		
		convertView = inflater.inflate(R.layout.choose_currency_row, null);
		ImageView flag = convertView.findViewById(R.id.chooseCurrencyFlag);
		TextView name = convertView.findViewById(R.id.chooseCurrencyName);
		
		flag.setImageResource(currency.getFlagIdentifier());
		name.setText(currency.getName());
		
		// Highlight selected item
		if (mSelectedItem != null) {
			int mSelectedPos = currencies.indexOf(mSelectedItem);
			if (position == mSelectedPos) {
				convertView.setBackgroundColor(context.getResources().getColor(R.color.navDrawerSelected));
				name.setTypeface(MainActivity.TYPEFACE_ROBOTO_BLACK);	
			}
		}
		
		return convertView;
	}
	
	/**
	 * Used to refresh the list of courses in the adapter
	 */
	public void updateCurrenciesList(){
		this.currencies = new ArrayList<>(preferences.getChosenCurrencies());
		notifyDataSetChanged();
	}
}
