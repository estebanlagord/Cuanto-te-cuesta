package com.smartpocket.cuantoteroban;

import java.text.NumberFormat;
import java.text.ParseException;

import com.smartpocket.cuantoteroban.editortype.EditorType;
import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class AmmountTextWatcher implements TextWatcher {

	public static TextView lastOneChanged = null;
	
	private static final int FRACTION_DIGITS = 2;
	private static final String VALUE_ARG = "value";
	private static MainActivity mainActivity;
	private static TextView ammountValue, discountValue, taxesValue, totalValue, pesosValue, creditCardValue, exchangeAgencyValue, payPalValue;
	private static NumberFormat nf = NumberFormat.getInstance();
	private static double bankExchangeRate;
	private static boolean invertBankExchangeRate;
	private static double bankExchangeRatePercentage;
	private static double agencyExchangeRate;
	private static boolean invertAgencyExchangeRate;
	private static double afipPercentage;
	private static double payPalPercentage;
	private static double discount;
	private static double taxes;

	private EditorType thisInstanceType;
	private boolean initialized = false;
	
	public AmmountTextWatcher(MainActivity mainActivity, EditorType editorType) {
		this.thisInstanceType = editorType;
		
		if (!initialized) {
			initialized = true;
			AmmountTextWatcher.mainActivity = mainActivity;
			
			nf.setMinimumFractionDigits(FRACTION_DIGITS);
			nf.setMaximumFractionDigits(FRACTION_DIGITS);
			
			ammountValue        = (TextView)mainActivity.findViewById(R.id.ammountEditText);
			discountValue       = (TextView)mainActivity.findViewById(R.id.discountEditText);
			taxesValue          = (TextView)mainActivity.findViewById(R.id.taxesEditText);
			totalValue          = (TextView)mainActivity.findViewById(R.id.totalEditText);
			pesosValue          = (TextView)mainActivity.findViewById(R.id.inPesosValue);
			creditCardValue     = (TextView)mainActivity.findViewById(R.id.withCreditCardValue);
			exchangeAgencyValue = (TextView)mainActivity.findViewById(R.id.exchangeAgencyValue);
			payPalValue         = (TextView)mainActivity.findViewById(R.id.payPalValue);
	
			preferencesChanged();
		}
	}

	public static synchronized void preferencesChanged() {
		bankExchangeRate           = PreferencesManager.getInstance().getBankExchangeRate();
		bankExchangeRatePercentage = PreferencesManager.getInstance().getBankCorrectionPercentage();
		agencyExchangeRate         = PreferencesManager.getInstance().getAgencyExchangeRate();
		afipPercentage             = PreferencesManager.getInstance().getAfipPercentage();
		payPalPercentage           = PreferencesManager.getInstance().getPayPalPercentage();
		invertBankExchangeRate     = PreferencesManager.getInstance().isBankExchangeRateInverted();
		invertAgencyExchangeRate   = PreferencesManager.getInstance().isAgencyExchangeRateInverted();
		discount                   = PreferencesManager.getInstance().getDiscount();
		taxes                      = PreferencesManager.getInstance().getTaxes();
		
		if (invertBankExchangeRate){
			if (bankExchangeRate != 0)
				bankExchangeRate = 1/bankExchangeRate;
		}
		
		if (invertAgencyExchangeRate){
			if (agencyExchangeRate != 0)
				agencyExchangeRate = 1/agencyExchangeRate;
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
		
	}

	@Override
	public synchronized void afterTextChanged(Editable ammountField) {
		mainActivity.disableEditTextListeners();

		if (ammountField.toString().length() > 0) {
			
			// turn "." into "0."
			if (ammountField.toString().startsWith(".")) {
				ammountField.insert(0, "0");
			}
			String valueStr = ammountField.toString();
			try {
				double number = nf.parse(valueStr).doubleValue();
				//Log.d("Ammount Text Watcher", "Changing " + thisInstanceType.toString() + " to " + number);

				switch (thisInstanceType) {
				case AMMOUNT:
					lastOneChanged = ammountValue;
					if(valueStr.length() > 0)
						updateValuesFromAmmount(number);
					else
						clearOtherTextViews(ammountValue);
					break;
				case DISCOUNT:
					updateValuesFromDiscount(number);
					break;
				case TAXES:
					updateValuesFromTaxes(number);
					break;
				case TOTAL:
					lastOneChanged = totalValue;
					if (valueStr.length() > 0)
						updateValuesFromTotal(number);
					else
						clearOtherTextViews(totalValue);
					break;
				case PESOS:
					lastOneChanged = pesosValue;
					if (valueStr.length() > 0)
						updateValuesFromPesos(number);
					else
						clearOtherTextViews(pesosValue);
					break;
				case CREDIT_CARD:
					lastOneChanged = creditCardValue;
					if (valueStr.length() > 0)
						updateValuesFromCreditCard(number);
					else
						clearOtherTextViews(creditCardValue);
					break;
				case EXCHANGE_AGENCY:
					lastOneChanged = exchangeAgencyValue;
					if (valueStr.length() > 0)
						updateValuesFromAgency(number);
					else
						clearOtherTextViews(exchangeAgencyValue);
					break;
				case PAYPAL:
					lastOneChanged = payPalValue;
					if (valueStr.length() > 0)
						updateValuesFromPayPal(number);
					else
						clearOtherTextViews(payPalValue);
					break;
				default:
					break;
				}
				
				updateDiscount();
				updateTaxes();
				
			} catch (ParseException e) {
				// unable to parse the input as a double
				DialogFragment dialog = new WrongNumberFormatDialog();
				Bundle args = new Bundle();
				args.putString(VALUE_ARG, valueStr);
				dialog.setArguments(args);
				dialog.show(mainActivity.getSupportFragmentManager(), "wrongnumber");
				clearOtherTextViews(null);
			}
		} else {
			clearOtherTextViews(null);
		}
		mainActivity.enableEditTextListeners();
	}
	
	private void clearOtherTextViews(TextView thisOne){
		if (thisOne != ammountValue)
			ammountValue.setText("");
		if (thisOne != pesosValue)
			pesosValue.setText("");
		if (thisOne != creditCardValue)
			creditCardValue.setText("");
		if (thisOne != exchangeAgencyValue)
			exchangeAgencyValue.setText("");
		if (thisOne != payPalValue)
			payPalValue.setText("");
	}

	private void updateValuesFromAmmount(double ammount) {
		double total = ammountToTotal(ammount);
		
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
	}
	
	private void updateValuesFromDiscount(double discount) {
		if (AmmountTextWatcher.discount != discount) {
			PreferencesManager.getInstance().setDiscount(discount);
			AmmountTextWatcher.discount = discount;
	
			if (lastOneChanged != null){
				// recalcular todo
				mainActivity.enableEditTextListeners();
				lastOneChanged.setText(lastOneChanged.getText());
			}
		}
	}
	
	private void updateValuesFromTaxes(double taxes) {
		if (AmmountTextWatcher.taxes != taxes){
			PreferencesManager.getInstance().setTaxes(taxes);
			AmmountTextWatcher.taxes = taxes;
	
			if (lastOneChanged != null){
				// recalcular todo
				mainActivity.enableEditTextListeners();
				lastOneChanged.setText(lastOneChanged.getText());
			}
		}
	}
	
	private void updateValuesFromTotal(double total) {
		updateAmmount(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
	}
	
	private void updateValuesFromPesos(double pesos) {
		double total = pesosToTotal(pesos);
		
		updateAmmount(total);
		updateTotal(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
	}
	
	private void updateValuesFromCreditCard(double creditCard) {
		double total = creditCardToTotal(creditCard);
		
		updateAmmount(total);
		updateTotal(total);
		updatePesos(total);
		updateAgency(total);
		updatePayPal(total);
	}
	
	private void updateValuesFromAgency(double agency) {
		double total = agencyToTotal(agency);
		
		updateAmmount(total);
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updatePayPal(total);
	}
	
	private void updateValuesFromPayPal(double payPal) {
		double total = payPalWithCreditCardToTotal(payPal);
		
		updateAmmount(total);
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
	}
	
	private void updateAmmount(double total){
		double ammount = totalToAmmount(total);
		ammount = Utilities.round(ammount, FRACTION_DIGITS);
		ammountValue.setText(nf.format(ammount));
	}
	
	private void updateDiscount() {
		double result = Utilities.round(discount, FRACTION_DIGITS);
		String resultStr = nf.format(result);
		if (!discountValue.getText().toString().equals(resultStr))
			discountValue.setText(nf.format(result));
	}
	
	private void updateTaxes() {
		double result = Utilities.round(taxes, FRACTION_DIGITS);
		String resultStr = nf.format(result);
		if (!taxesValue.getText().toString().equals(resultStr))
			taxesValue.setText(nf.format(result));
	}
	
	private void updateTotal(double total){
		totalValue.setText(nf.format(Utilities.round(total, FRACTION_DIGITS)));
	}
	
	private void updatePesos(double total){
		double pesos = totalToPesos(total);
		pesos = Utilities.round(pesos, FRACTION_DIGITS);
		pesosValue.setText(nf.format(pesos));
	}
	
	private void updateCreditCard(double total){
		double creditCard = totalToCreditCard(total);
		creditCard = Utilities.round(creditCard, FRACTION_DIGITS);
		creditCardValue.setText(nf.format(creditCard));
	}
	
	private void updateAgency(double total){
		double agency = totalToAgency(total);
		agency = Utilities.round(agency, FRACTION_DIGITS);
		exchangeAgencyValue.setText(nf.format(agency));
	}
	
	private void updatePayPal(double total){
		double payPalWithCreditCard = totalToPayPalWithCreditCard(total);
		payPalWithCreditCard = Utilities.round(payPalWithCreditCard, FRACTION_DIGITS);
		payPalValue.setText(nf.format(payPalWithCreditCard));
	}
	
	private double totalToAmmount(double total) {
		double totalWithoutTaxes = total / (1 + taxes / 100);
		//double totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100); //THIS IS SOMETIMES  DIVIDING BY 0
		double totalWithoutTaxesOrDiscounts = 0;
		if (discount != 100)
			totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100);
		
		return totalWithoutTaxesOrDiscounts;
	}
	
	private double ammountToTotal(double ammount) {
		double ammountWithDiscount = ammount - (discount * ammount / 100);
		double ammountWithDiscountAfterTaxes = ammountWithDiscount + (taxes * ammountWithDiscount / 100);
		return ammountWithDiscountAfterTaxes;
	}
	
	private double totalToPesos(double total) {
		double pesos = total * bankExchangeRate;
		pesos = pesos + (bankExchangeRatePercentage * pesos / 100); // suma porcentaje de correccion de la cotizacion

		return pesos;
	}
	
	private double pesosToTotal(double pesos) {
		if (bankExchangeRate == 0) return 0;
		
		double total = pesos - (bankExchangeRatePercentage * pesos / 100); // resta la correccion
		total = total / bankExchangeRate;
		return total;
	}
	
	private double totalToCreditCard(double total){
		double pesos = totalToPesos(total);
		double withCreditCard = pesos + (afipPercentage * pesos / 100);  // suma 15%
		return withCreditCard;
	}
	
	private double creditCardToTotal(double creditCard){
		double pesos = creditCard / (1 + afipPercentage / 100);
		double total = pesosToTotal(pesos);
		return total;
	}
	
	private double totalToAgency(double total) {
		double agency = total * agencyExchangeRate;
		return agency;
	}
	
	private double agencyToTotal(double agency) {
		if (agencyExchangeRate == 0) return 0;
		
		double total = agency / agencyExchangeRate;
		return total;
	}
	
	private double totalToPayPalWithCreditCard(double total) {
		double pesos = totalToPesos(total);
		double payPalOnly = pesos + (payPalPercentage * pesos / 100);
		double payPalWithCreditCard = payPalOnly + (payPalOnly * afipPercentage / 100);
		return payPalWithCreditCard;
	}
	
	private double payPalWithCreditCardToTotal(double payPalWithCreditCard) {
		double payPalOnly = payPalWithCreditCard / (1 + afipPercentage / 100);  //restar 35% de lo original
		double pesos = payPalOnly / (1 + payPalPercentage / 100);
		double total = pesosToTotal(pesos);
		return total;
	}
	

	
	public static class WrongNumberFormatDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String value = getArguments().getString(VALUE_ARG);
			
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("El valor ingresado no es un número válido: " + value)
	               .setPositiveButton("Aceptar", null);

	        return builder.create();
		}
	}
}