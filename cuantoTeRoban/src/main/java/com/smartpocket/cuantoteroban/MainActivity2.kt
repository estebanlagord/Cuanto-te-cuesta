package com.smartpocket.cuantoteroban

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.smartpocket.cuantoteroban.MainActivity.FRACTION_DIGITS
import com.smartpocket.cuantoteroban.MainActivity.RequestCode
import com.smartpocket.cuantoteroban.calc.Calculator
import com.smartpocket.cuantoteroban.editortype.EditorType
import com.smartpocket.cuantoteroban.editortype.EditorTypeHelper
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat

class MainActivity2 : AppCompatActivity() {

    private lateinit var viewModel: MainActivityVM
    private val shortNumberFormat = (DecimalFormat.getInstance() as DecimalFormat).apply {
        setMinimumFractionDigits(FRACTION_DIGITS)
        setMaximumFractionDigits(FRACTION_DIGITS)
        positivePrefix = "$ "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_main)

        setupViewModel()
        setupClickListeners()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainActivityVM::class.java)
        viewModel.officialLiveData.observe(this, Observer { officialValue: Double ->
            showValue(officialValue, inPesosValue)
        })
        viewModel.cardLiveData.observe(this, Observer { cardValue: Double ->
            showValue(cardValue, withCreditCardValue)
        })
        viewModel.blueLiveData.observe(this, Observer { blueValue: Double ->
            showValue(blueValue, withBlueValue)
        })
    }

    private fun showValue(value: Double, textView: TextView) {
        textView.setText(shortNumberFormat.format(value))
    }

    private fun setupClickListeners() {
        amountEditText.setOnClickListener(OnClickListenerShowCalc(amountEditText, resources.getString(R.string.Ammount), EditorType.AMOUNT))
        discountEditText.setOnClickListener(OnClickListenerShowCalc(discountEditText, resources.getString(R.string.Discount), EditorType.DISCOUNT))
        taxesEditText.setOnClickListener(OnClickListenerShowCalc(taxesEditText, resources.getString(R.string.Taxes), EditorType.TAXES))
        totalEditText.setOnClickListener(OnClickListenerShowCalc(totalEditText, resources.getString(R.string.Total), EditorType.TOTAL))
        inPesosValue.setOnClickListener(OnClickListenerShowCalc(inPesosValue, resources.getString(R.string.InPesos), EditorType.PESOS))
        withCreditCardValue.setOnClickListener(OnClickListenerShowCalc(withCreditCardValue, resources.getString(R.string.WithCreditCard), EditorType.CREDIT_CARD))
        withSavingsValue.setOnClickListener(OnClickListenerShowCalc(withSavingsValue, resources.getString(R.string.WithSavings), EditorType.SAVINGS))
        withBlueValue.setOnClickListener(OnClickListenerShowCalc(withBlueValue, resources.getString(R.string.WithBlue), EditorType.BLUE))
    }

    inner class OnClickListenerShowCalc(private val editText: EditText, editTextName: String, private val editorType: EditorType) : View.OnClickListener {
        private val editTextName: String
        override fun onClick(v: View) {
//            currentEditTextBeingEdited = editText
            //currentEditTextBeingEdited_Name = editTextName;
//            if (mActionMode != null) mActionMode.finish()
            val calc = Intent(this@MainActivity2, Calculator::class.java)
            calc.putExtra("editTextValue", editText.text.toString())
            calc.putExtra("editTextName", editTextName)
            calc.putExtra("type", editorType.name)
            startActivityForResult(calc, RequestCode.CALCULATOR.ordinal)
        }

        init {
            this.editTextName = editTextName.replace(":", "")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.CALCULATOR.ordinal) {
            if (resultCode == Activity.RESULT_OK) {
                val editorTypeName = data!!.extras!!.getString(Calculator.RESULT_TYPE)
                val editorType = EditorTypeHelper.getEditorType(editorTypeName)
                val targetEditText = EditorTypeHelper.getEditTextForEditorType(this, editorType)
                var newValue = data.extras!!.getDouble(Calculator.RESULT)
                newValue = Utilities.round(newValue, FRACTION_DIGITS)
                val valueStr: String = shortNumberFormat.format(newValue)
                targetEditText.setText(valueStr)

                when (editorType) {
                    EditorType.AMOUNT -> viewModel.onAmountValueChanged(newValue)
                }

//                updateEditTextBackgrounds(targetEditText)
                // save which EditText was edited
                if (targetEditText !== discountEditText
                        && targetEditText !== taxesEditText) {
                    PreferencesManager.getInstance().lastConversionType = editorType
                    PreferencesManager.getInstance().lastConversionValue = newValue
                }
//                updateDeleteTaxOrDiscountVisibility()
            }
        }
    }
}
