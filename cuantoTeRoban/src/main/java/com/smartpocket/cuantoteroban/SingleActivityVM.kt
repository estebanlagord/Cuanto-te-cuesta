package com.smartpocket.cuantoteroban

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.preferences.PreferencesManager

class SingleActivityVM : ViewModel() {

    val preferences = PreferencesManager.getInstance()

    val calculatorResultLD = SingleLiveEvent<CalculatorResult>()
    val showAdsLD = MutableLiveData<Boolean>(preferences.isRemoveAdsPurchased.not())
    val billingStatusLD = SingleLiveEvent<Int>()
    val launchPurchaseLD = SingleLiveEvent<Boolean>()
    val launchRestoreAdsLD = SingleLiveEvent<Boolean>()
    val snackbarLD = SingleLiveEvent<String>()
    val addedCurrencyLD = SingleLiveEvent<Currency>()

    class CalculatorResult(val amount: Double, val editorType: EditorType)
}