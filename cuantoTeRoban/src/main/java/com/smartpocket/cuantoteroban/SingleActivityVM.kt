package com.smartpocket.cuantoteroban

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SingleActivityVM @Inject constructor() : ViewModel() {

    @Inject
    lateinit var preferences: PreferencesManager

    val calculatorResultLD = SingleLiveEvent<CalculatorResult>()
    val showAdsLD by lazy { MutableLiveData(preferences.isRemoveAdsPurchased.not()) }
    val billingStatusLD = SingleLiveEvent<Int>()
    val launchPurchaseLD = SingleLiveEvent<Boolean>()
    val launchRestoreAdsLD = SingleLiveEvent<Boolean>()
    val snackbarLD = SingleLiveEvent<String>()
    val addedCurrencyLD = SingleLiveEvent<Currency>()

    class CalculatorResult(val amount: Double, val editorType: EditorType)
}