package com.smartpocket.cuantoteroban.repository.graph

import com.github.kittinunf.fuel.core.FuelError
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.MyApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(application = MyApplication::class)
class GraphDataProviderTest {

    private lateinit var graphDataProvider: GraphDataProvider
    private val currencyManager = CurrencyManager()

    @Before
    fun setUp() {
        graphDataProvider = GraphDataProvider()
    }

    @Test
    fun getGraphData() = runBlocking {
        val listOK = mutableListOf<String>()
        val listError = mutableListOf<Currency>()
        val dateRange = Date(0)..Date()

        currencyManager.allCurrencies.forEach {
            try {
                println("Testing graph data for ${it.name}")
                val data = graphDataProvider.getGraphData(it, CurrencyManager.ARS, dateRange)
                assertFalse(data.isEmpty())
                listOK.add(it.code)
            } catch (e: Exception) {
                println("Error getting graph data for ${it.code}")
                if (e is FuelError && e.response.statusCode == 404) {
                    // 404 not found is expected for some currencies
                } else {
                    e.printStackTrace()
                    fail("Unexpected exception")
                }
                listError.add(it)
            }
        }
        println("Tested graph data for ${currencyManager.allCurrencies.size} currencies")
        println("Downloaded ${listOK.size} OK: $listOK.")
        if (listError.isNotEmpty()) {
            println("Downloaded ${listError.size} with ERROR: ${listError.map { it.code }}")
            println("Currencies with error:")
            listError.forEach {
                println("\t${it.name} - ${it.country}")
            }
        }
        assertFalse("All currencies failed to download graph data!", listOK.isEmpty())
        assertTrue("Some currencies failed to download graph data!", listError.isEmpty())
        println("All currencies downloaded graph data OK!")
    }
}