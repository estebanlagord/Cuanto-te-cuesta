package com.smartpocket.cuantoteroban;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.smartpocket.cuantoteroban.editortype.EditorType;
import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

import java.text.NumberFormat;
import java.text.ParseException;

public class AmountTextWatcher implements TextWatcher {

	public static TextView lastOneChanged = null;
	
	private static final int FRACTION_DIGITS = 2;
	private static final String VALUE_ARG = "value";
	private static MainActivity mainActivity;
	private static TextView amountValue, discountValue, taxesValue, totalValue, pesosValue, savingsValue, creditCardValue, blueValue, exchangeAgencyValue, payPalValue;
	private static final NumberFormat nf = NumberFormat.getInstance();
	private static double bankExchangeRate;
	private static boolean invertBankExchangeRate;
	private static double bankExchangeRatePercentage;
	private static double agencyExchangeRate;
	private static boolean invertAgencyExchangeRate;
    private static double savingsPercentage;
    private static double afipPercentage;
	private static double payPalPercentage;
	private static double discount;
	private static double taxes;

	private final EditorType thisInstanceType;
	private boolean initialized = false;
	
	public AmountTextWatcher(MainActivity mainActivity, EditorType editorType) {
		this.thisInstanceType = editorType;
		
		if (!initialized) {
			initialized = true;
			AmountTextWatcher.mainActivity = mainActivity;
			
			nf.setMinimumFractionDigits(FRACTION_DIGITS);
			nf.setMaximumFractionDigits(FRACTION_DIGITS);
			
			amountValue         = mainActivity.findViewById(R.id.amountEditText);
			discountValue       = mainActivity.findViewById(R.id.discountEditText);
			taxesValue          = mainActivity.findViewById(R.id.taxesEditText);
			totalValue          = mainActivity.findViewById(R.id.totalEditText);
			pesosValue          = mainActivity.findViewById(R.id.inPesosValue);
//            savingsValue        = mainActivity.findViewById(R.id.withSavingsValue);
            blueValue           = mainActivity.findViewById(R.id.withBlueValue);
			creditCardValue     = mainActivity.findViewById(R.id.withCreditCardValue);
			exchangeAgencyValue = mainActivity.findViewById(R.id.exchangeAgencyValue);
//			payPalValue         = mainActivity.findViewById(R.id.payPalValue);

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
        savingsPercentage          = PreferencesManager.getInstance().getSavingsPercentage();

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
	public synchronized void afterTextChanged(Editable amountField) {
		mainActivity.disableEditTextListeners();

		if (amountField.toString().length() > 0) {
			
			// turn "." into "0."
			if (amountField.toString().startsWith(".")) {
				amountField.insert(0, "0");
			}
			String valueStr = amountField.toString();
			try {
				double number = nf.parse(valueStr).doubleValue();
				//Log.d("Amount Text Watcher", "Changing " + thisInstanceType.toString() + " to " + number);

				switch (thisInstanceType) {
				case AMOUNT:
					lastOneChanged = amountValue;
					if(valueStr.length() > 0)
						updateValuesFromAmount(number);
					else
						clearOtherTextViews(amountValue);
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
                case SAVINGS:
                    lastOneChanged = savingsValue;
                    if (valueStr.length() > 0)
                        updateValuesFromSavings(number);
                    else
                        clearOtherTextViews(savingsValue);
                    break;
                case BLUE:
                    lastOneChanged = blueValue;
                    if (valueStr.length() > 0)
                        updateValuesFromBlue(number);
                    else
                        clearOtherTextViews(blueValue);
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
		if (thisOne != amountValue)
			amountValue.setText("");
		if (thisOne != pesosValue)
			pesosValue.setText("");
		if (thisOne != creditCardValue)
			creditCardValue.setText("");
		if (thisOne != exchangeAgencyValue)
			exchangeAgencyValue.setText("");
		if (thisOne != payPalValue)
			payPalValue.setText("");
        if (thisOne != savingsValue)
            savingsValue.setText("");
        if (thisOne != blueValue)
            blueValue.setText("");
	}

	private void updateValuesFromAmount(double amount) {
		double total = amountToTotal(amount);
		
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
        updateSavings(total);
        updateBlue(total);
	}
	
	private void updateValuesFromDiscount(double discount) {
		if (AmountTextWatcher.discount != discount) {
			PreferencesManager.getInstance().setDiscount(discount);
			AmountTextWatcher.discount = discount;
	
			if (lastOneChanged != null){
				// recalculate everything
				mainActivity.enableEditTextListeners();
				lastOneChanged.setText(lastOneChanged.getText());
			}
		}
	}
	
	private void updateValuesFromTaxes(double taxes) {
		if (AmountTextWatcher.taxes != taxes){
			PreferencesManager.getInstance().setTaxes(taxes);
			AmountTextWatcher.taxes = taxes;
	
			if (lastOneChanged != null){
                // recalculate everything
				mainActivity.enableEditTextListeners();
				lastOneChanged.setText(lastOneChanged.getText());
			}
		}
	}
	
	private void updateValuesFromTotal(double total) {
		updateAmount(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
        updateSavings(total);
        updateBlue(total);
	}
	
	private void updateValuesFromPesos(double pesos) {
		double total = pesosToTotal(pesos);
		
		updateAmount(total);
		updateTotal(total);
		updateCreditCard(total);
		updateAgency(total);
		updatePayPal(total);
        updateSavings(total);
        updateBlue(total);
	}
	
	private void updateValuesFromCreditCard(double creditCard) {
		double total = creditCardToTotal(creditCard);
		
		updateAmount(total);
		updateTotal(total);
		updatePesos(total);
		updateAgency(total);
		updatePayPal(total);
        updateSavings(total);
        updateBlue(total);
	}

    private void updateValuesFromSavings(double savings) {
        double total = savingsToTotal(savings);

        updateAmount(total);
        updateTotal(total);
        updatePesos(total);
        updateCreditCard(total);
        updateAgency(total);
        updatePayPal(total);
        updateBlue(total);
    }

    private void updateValuesFromBlue(double blue) {
        double total = blueToTotal(blue);

        updateAmount(total);
        updateTotal(total);
        updatePesos(total);
        updateCreditCard(total);
        updateAgency(total);
        updatePayPal(total);
        updateSavings(total);
    }
	
	private void updateValuesFromAgency(double agency) {
		double total = agencyToTotal(agency);
		
		updateAmount(total);
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updatePayPal(total);
        updateSavings(total);
        updateBlue(total);
	}
	
	private void updateValuesFromPayPal(double payPal) {
		double total = payPalWithCreditCardToTotal(payPal);
		
		updateAmount(total);
		updateTotal(total);
		updatePesos(total);
		updateCreditCard(total);
		updateAgency(total);
        updateSavings(total);
        updateBlue(total);
	}
	
	private void updateAmount(double total){
		double amount = totalToAmount(total);
		amount = Utilities.round(amount, FRACTION_DIGITS);
		amountValue.setText(nf.format(amount));
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

    private void updateSavings(double total){
        double savings = totalToSavings(total);
        savings = Utilities.round(savings, FRACTION_DIGITS);
        savingsValue.setText(nf.format(savings));
    }

    private void updateBlue(double total){
        double blue = totalToBlue(total);
        blue = Utilities.round(blue, FRACTION_DIGITS);
        blueValue.setText(nf.format(blue));
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
	
	private double totalToAmount(double total) {
		double totalWithoutTaxes = total / (1 + taxes / 100);
		//double totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100); //THIS IS SOMETIMES  DIVIDING BY 0
		double totalWithoutTaxesOrDiscounts = 0;
		if (discount != 100)
			totalWithoutTaxesOrDiscounts = totalWithoutTaxes / (1 - discount / 100);
		
		return totalWithoutTaxesOrDiscounts;
	}
	
	private double amountToTotal(double amount) {
		double amountWithDiscount = amount - (discount * amount / 100);
		double amountWithDiscountAfterTaxes = amountWithDiscount + (taxes * amountWithDiscount / 100);
		return amountWithDiscountAfterTaxes;
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
		double withCreditCard = pesos + (afipPercentage * pesos / 100);  // suma 35 de AFIP%
		return withCreditCard;
	}
	
	private double creditCardToTotal(double creditCard){
		double pesos = creditCard / (1 + afipPercentage / 100);
		double total = pesosToTotal(pesos);
		return total;
	}

    private double totalToSavings(double total){
        double pesos = totalToPesos(total);
        double withSavings = pesos + (savingsPercentage * pesos / 100); // suma 20% de ahorro
        return withSavings;
    }

    private double savingsToTotal(double savings){
        double pesos = savings / (1 + savingsPercentage / 100);  // quita el 20% de ahorro
        double total = pesosToTotal(pesos);
        return total;
    }

    private double totalToBlue(double total){
        double toDollarRate = PreferencesManager.getInstance().getExchangeRateToDollar();
        double blueRate = PreferencesManager.getInstance().getBlueDollarToARSRate();
        double result = total * toDollarRate * blueRate;
        return result;
    }

    private double blueToTotal(double blue){
        double toDollarRate = PreferencesManager.getInstance().getExchangeRateToDollar();
        double blueRate = PreferencesManager.getInstance().getBlueDollarToARSRate();
        double result = blue / blueRate / toDollarRate;
        return result;
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