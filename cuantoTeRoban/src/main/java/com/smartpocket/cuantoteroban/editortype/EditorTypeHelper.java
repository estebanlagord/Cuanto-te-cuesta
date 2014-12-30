package com.smartpocket.cuantoteroban.editortype;

import android.app.Activity;
import android.widget.EditText;

import com.smartpocket.cuantoteroban.R;
import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

public class EditorTypeHelper {
	public static EditText getEditTextForEditorType(Activity activity, EditorType type) {
		int id = 0;
		
		switch (type) {
			case AMOUNT:
				id = R.id.amountEditText;
				break;
			case DISCOUNT:
				id = R.id.discountEditText;
				break;
			case CREDIT_CARD:
				id = R.id.withCreditCardValue;
				break;
            case SAVINGS:
                id = R.id.withSavingsValue;
                break;
            case BLUE:
                id = R.id.withBlueValue;
                break;
			case EXCHANGE_AGENCY:
				id = R.id.exchangeAgencyValue;
				break;
			case PAYPAL:
				id = R.id.payPalValue;
				break;
			case PESOS:
				id = R.id.inPesosValue;
				break;
			case TAXES:
				id = R.id.taxesEditText;
				break;
			case TOTAL:
				id = R.id.totalEditText;
				break;
		}
		
		EditText result = (EditText) activity.findViewById(id);
		return result;
	}

	public static EditorType getEditorType(String typeName) {
		EditorType result = EditorType.valueOf(EditorType.class, typeName);
		return result;
	}
	
	public static EditText getLastConversionEditText(Activity activity) {
		EditorType type = PreferencesManager.getInstance().getLastConversionType();
		EditText result = getEditTextForEditorType(activity, type);
		return result;
	}
}
