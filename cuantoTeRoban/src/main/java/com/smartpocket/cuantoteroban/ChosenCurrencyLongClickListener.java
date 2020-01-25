package com.smartpocket.cuantoteroban;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ChosenCurrencyLongClickListener implements OnItemLongClickListener {

	private Fragment parent;
	private final DeleteCurrencyDialogListener listener;

	public ChosenCurrencyLongClickListener(Fragment parent) {
		super();
		this.parent = parent;
		this.listener = (DeleteCurrencyDialogListener) parent;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
		//@SuppressWarnings("unchecked")
		//HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
		//String currCode = (String) map.get(COLUMN_NAMES.CODE.name());
		Currency curr = (Currency) listView.getItemAtPosition(position);

		DialogFragment dialogFragment = DeleteCurrencyDialogFragment.newInstance(curr.getCode(), listener);
		dialogFragment.show(parent.getChildFragmentManager(), "deleteCurrency");

		return true;
	}


	public static class DeleteCurrencyDialogFragment extends DialogFragment {
		static final String CURR_CODE = "currencyCode";
		Currency currency;
		DeleteCurrencyDialogListener mListener;

		static DeleteCurrencyDialogFragment newInstance(String currCode, DeleteCurrencyDialogListener listener)
		{
			DeleteCurrencyDialogFragment fragment = new DeleteCurrencyDialogFragment();
		    Bundle bundle = new Bundle(1);
		    bundle.putString(CURR_CODE, currCode);
		    fragment.setArguments(bundle);
		    fragment.mListener = listener;
		    return fragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			String currCode = getArguments().getString(CURR_CODE);
			this.currency = CurrencyManager.getInstance().findCurrency(currCode);
		}

	    @NotNull
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle("¿Borrar?")
	        		.setMessage("¿Queres borrar la moneda " + currency.getName() + " de esta lista?")
	        		.setNegativeButton("Cancelar", null)
	        		.setPositiveButton("OK", (dialog, id) -> {
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
                    });

	        return builder.create();
	    }
	}
}

