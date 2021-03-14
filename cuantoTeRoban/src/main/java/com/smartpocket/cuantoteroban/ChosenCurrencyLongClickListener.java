package com.smartpocket.cuantoteroban;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ChosenCurrencyLongClickListener implements OnItemLongClickListener {

	private final Fragment parent;
	private final DeleteCurrencyDialogListener listener;
	private final PreferencesManager preferences;
	private final CurrencyManager currencyManager;

	public ChosenCurrencyLongClickListener(Fragment parent, PreferencesManager preferences, CurrencyManager currencyManager) {
		super();
		this.parent = parent;
		this.preferences = preferences;
		this.currencyManager = currencyManager;
		this.listener = (DeleteCurrencyDialogListener) parent;
	}

	public boolean onItemLongClick(Currency curr) {
		DialogFragment dialogFragment = DeleteCurrencyDialogFragment.newInstance(curr.getCode(), listener, preferences, currencyManager);
		dialogFragment.show(parent.getParentFragmentManager(), "deleteCurrency");
		return true;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
		//@SuppressWarnings("unchecked")
		//HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
		//String currCode = (String) map.get(COLUMN_NAMES.CODE.name());
		Currency curr = (Currency) listView.getItemAtPosition(position);
		return onItemLongClick(curr);
	}


	public static class DeleteCurrencyDialogFragment extends DialogFragment {
		static final String CURR_CODE = "currencyCode";
		Currency currency;
		DeleteCurrencyDialogListener mListener;
		PreferencesManager preferences;
		CurrencyManager currencyManager;

		static DeleteCurrencyDialogFragment newInstance(String currCode,
														DeleteCurrencyDialogListener listener,
														PreferencesManager preferences,
														CurrencyManager currencyManager)
		{
			DeleteCurrencyDialogFragment fragment = new DeleteCurrencyDialogFragment();
		    Bundle bundle = new Bundle(1);
		    bundle.putString(CURR_CODE, currCode);
		    fragment.setArguments(bundle);
		    fragment.mListener = listener;
		    fragment.preferences = preferences;
		    fragment.currencyManager = currencyManager;
		    return fragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			String currCode = getArguments().getString(CURR_CODE);
			this.currency = currencyManager.findCurrency(currCode);
		}

	    @NotNull
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
					.setTitle("¿Borrar?")
	        		.setMessage("¿Queres borrar la moneda " + currency.getName() + " de esta lista?")
	        		.setNegativeButton("Cancelar", null)
	        		.setPositiveButton("Borrar", (dialog, id) -> {
                        Currency currentCurrency = preferences.getCurrentCurrency();
                        if (currency.equals(currentCurrency)) {
                            MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(requireContext());
                            builder2.setMessage("No se puede borrar la moneda " + currency.getName() + " porque es la que se está mostrando ahora en la pantalla principal.");
                            builder2.setNeutralButton("OK", null);
                            Dialog d = builder2.create();
                            d.show();
                        } else {
                            List<Currency> chosenCurrencies = preferences.getChosenCurrencies();
                            chosenCurrencies.remove(currency);
							preferences.setChosenCurrencies(chosenCurrencies);
                            mListener.onDialogPositiveClick(DeleteCurrencyDialogFragment.this);
                        }
                    });

	        return builder.create();
	    }
	}
}

