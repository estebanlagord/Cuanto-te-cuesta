package com.smartpocket.cuantoteroban

import androidx.lifecycle.ViewModel
import com.smartpocket.cuantoteroban.editortype.EditorType

class SingleActivityVM : ViewModel() {

    val calculatorResultLD = SingleLiveEvent<CalculatorResult>()

    class CalculatorResult(val amount: Double, val editorType: EditorType)
}