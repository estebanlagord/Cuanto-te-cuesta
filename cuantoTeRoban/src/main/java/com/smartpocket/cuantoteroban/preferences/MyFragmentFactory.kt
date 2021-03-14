package com.smartpocket.cuantoteroban.preferences

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.PreferencesFragment.MyPreferenceFragment
import com.smartpocket.cuantoteroban.preferences.PreferencesFragmentForCurrency.MyPreferenceForCurrencyFragment

class MyFragmentFactory(private val preferencesManager: PreferencesManager,
                        private val currencyManager: CurrencyManager)
    : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
            when (loadFragmentClass(classLoader, className)) {
                MyPreferenceFragment::class.java -> MyPreferenceFragment(preferencesManager, currencyManager)
                MyPreferenceForCurrencyFragment::class.java -> MyPreferenceForCurrencyFragment(preferencesManager)
                else -> super.instantiate(classLoader, className)
            }

}