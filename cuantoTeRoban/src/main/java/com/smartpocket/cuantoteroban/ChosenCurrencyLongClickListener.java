package com.smartpocket.cuantoteroban;

import java.util.List;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;


public class ChosenCurrencyLongClickListener implements OnItemLongClickListener {

	private FragmentActivity parent;
	
	public ChosenCurrencyLongClickListener(FragmentActivity parent) {
		super();
		this.parent = parent;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
		//@SuppressWarnings("unchecked")
		//HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
		//String currCode = (String) map.get(COLUMN_NAMES.CODE.name());
		Currency curr = (Currency) listView.getItemAtPosition(position);
		
		DialogFragment dialogFragment = DeleteCurrencyDialogFragment.newInstance(curr.getCode());
		dialogFragment.show(parent.getSupportFragmentManager(), "deleteCurrency");
		
		return true;
	}

	
	public static class DeleteCurrencyDialogFragment extends DialogFragment {
		public static final String CURR_CODE = "currencyCode";
		Currency currency;
		DeleteCurrencyDialogListener mListener;
		
		public static final DeleteCurrencyDialogFragment newInstance(String currCode)
		{
			DeleteCurrencyDialogFragment fragment = new DeleteCurrencyDialogFragment();
		    Bundle bundle = new Bundle(1);
		    bundle.putString(CURR_CODE, currCode);
		    fragment.setArguments(bundle);
		    return fragment;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			String currCode = getArguments().getString(CURR_CODE);
			this.currency = CurrencyManager.getInstance().findCurrency(currCode);
		}
		
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	            mListener = (DeleteCurrencyDialogListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
	        }
	    }

		
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle("¿Borrar?")
	        		.setMessage("¿Queres borrar la moneda " + currency.getName() + " de esta lista?")
	        		.setNegativeButton("Cancelar", null)
	        		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   Currency currentCurrency = PreferencesManager.getInstance().getCurrentCurrency();
	                	   if (currency.equals(currentCurrency)) {
	                		   AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
	                		   builder2.setMessage("No se puede borrar la moneda " + currency.getName() + " porque es la que se está mostrando ahora en la pantalla principal.");
	                		   builder2.setNeutralButton("OK", null);
	                		   Dialog d = builder2.create();
	                		   d.show();
	                	   } else {
		                       List<Currency> chosenCurrencies = PreferencesManager.getInstance().getChosenCurrencies();
		                       chosenCurrencies.remove(currency);
		                	   PreferencesManager.getInstance().setChosenCurrencies(chosenCurrencies);
		                	   mListener.onDialogPositiveClick(DeleteCurrencyDialogFragment.this);
	                	   }
	                   }
	               });

	        return builder.create();
	    }
	}
}

interface DeleteCurrencyDialogListener {
    public void onDialogPositiveClick(DialogFragment dialog);
}