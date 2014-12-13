package com.smartpocket.cuantoteroban;

import java.util.ArrayList;
import java.util.List;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChosenCurrenciesAdapter extends BaseAdapter {
	Context context;
	List<Currency> currencies;
	LayoutInflater inflater;
	/** Selected item position	*/
	private Currency mSelectedItem;
	
	
	public ChosenCurrenciesAdapter(Context context) {
		this.context = context;
		this.currencies = new ArrayList<Currency>(CurrencyManager.getInstance().getUserCurrencies());
		this.inflater = ((Activity)context).getLayoutInflater();
		
		updateSelectedItem();
	}
	
	public Currency getSelectedItem() {
		return mSelectedItem;
	}
	
	public void setSelectedItem(Currency selectedItem) {
		mSelectedItem = selectedItem;
	}
	
	public void updateSelectedItem() {
		Currency currentCurr = PreferencesManager.getInstance().getCurrentCurrency();
		setSelectedItem(currentCurr);
		notifyDataSetChanged();
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
		ImageView flag = (ImageView) convertView.findViewById(R.id.chooseCurrencyFlag);
		TextView name = (TextView) convertView.findViewById(R.id.chooseCurrencyName);
		
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
		this.currencies = new ArrayList<Currency>(CurrencyManager.getInstance().getUserCurrencies());
		notifyDataSetChanged();
	}
}
