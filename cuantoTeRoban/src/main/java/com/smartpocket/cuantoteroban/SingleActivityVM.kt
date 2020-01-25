package com.smartpocket.cuantoteroban

import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType

class SingleActivityVM : ViewModel() {

    val calculatorResultLD = SingleLiveEvent<CalculatorResult>()
    val billingStatusLD = SingleLiveEvent<Int>()

    class CalculatorResult(val amount: Double, val editorType: EditorType)
}