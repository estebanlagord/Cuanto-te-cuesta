package com.smartpocket.cuantoteroban

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_single.*

class SingleActivity : AppCompatActivity() {

    private lateinit var adViewHelper: AdViewHelper
//    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)
//        val toolbar = toolbar as Toolbar
//        setSupportActionBar(toolbar)
//        val navController = findNavController(R.id.nav_host_fragment)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        toolbar.setupWithNavController(navController, appBarConfiguration)
//        setupActionBarWithNavController(this, navController)
        adViewHelper = AdViewHelper(adViewContainer, this)
    }

    override fun onResume() {
        super.onResume()
        val isAdFree = MyApplication.billingHelper().isRemoveAdsPurchased()
        adViewHelper.resume(isAdFree)
    }

    override fun onPause() {
        adViewHelper.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adViewHelper.destroy()
        super.onDestroy()
    }

    override fun onSupportNavigateUp() =
            findNavController(R.id.nav_host_fragment).navigateUp()
}
