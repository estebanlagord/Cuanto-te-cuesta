package com.smartpocket.cuantoteroban.repository

import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.MyApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = MyApplication::class)
class CurrencyRemoteRepositoryTest {

    private lateinit var repository: CurrencyRemoteRepository
    private val currencyManager = CurrencyManager.getInstance()

    @Before
    fun setUp() {
        repository = CurrencyRemoteRepository()
    }

    @Test
    fun getRatesForAllCurrencies() = runBlocking {
        val listOK = mutableListOf<String>()
        val listError = mutableListOf<Currency>()
        currencyManager.allCurrencies.forEach {
            try {
                println("Testing ${it.name}")
                val result = repository.getCurrencyExchange(it, CurrencyManager.ARS, 1.0)
                assertTrue("Rate for ${it.code} cannot be $0", result.official > 0)
                if (result is DolarResult) {
                    assertTrue("Blue rate for USD cannot be $0", result.blue > 0)
                }
                listOK.add(it.code)
            } catch (e: Exception) {
                println("Error getting currency for ${it.code}")
                e.printStackTrace()
                listError.add(it)
            }
        }
        println("Tested ${currencyManager.allCurrencies.size} currencies")
        println("Downloaded ${listOK.size} OK: $listOK.")
        if (listError.isNotEmpty()) {
            println("Downloaded ${listError.size} with ERROR: ${listError.map { it.code }}")
            println("Currencies with error:")
            listError.forEach {
                println("\t${it.name} - ${it.country}")
            }
        }
        assertFalse("All currencies failed to download!", listOK.isEmpty())
        assertTrue("Some currencies failed to download!", listError.isEmpty())
        println("All currencies downloaded OK!")
    }
}