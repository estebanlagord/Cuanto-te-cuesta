package com.smartpocket.cuantoteroban.search

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.smartpocket.cuantoteroban.Currency
import com.smartpocket.cuantoteroban.R
import com.smartpocket.cuantoteroban.SingleActivityVM
import com.smartpocket.cuantoteroban.databinding.ActivityAddCurrencyBinding
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddCurrencyFragment : Fragment(), OnCurrencyItemClickListener {

    private var _binding: ActivityAddCurrencyBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var unusedCurrencies: Set<Currency>
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: CurrencyListAdapter
    private lateinit var singleActivityVM: SingleActivityVM

    @Inject
    lateinit var preferences: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityAddCurrencyBinding.inflate(inflater, container, false)
        val toolbar: Toolbar = binding.toolbar.myAwesomeToolbar
        with(requireActivity() as AppCompatActivity) {
            toolbar.title = getString(R.string.title_activity_add_currency)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        unusedCurrencies = preferences.allUnusedCurrencies
        mAdapter = CurrencyListAdapter(this)
        recyclerView = binding.unusedCurrenciesList
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL))
        FastScrollerBuilder(recyclerView).useMd2Style().build()
        setHasOptionsMenu(true)
        updateCurrenciesList(null)
        singleActivityVM = ViewModelProvider(requireActivity())[SingleActivityVM::class.java]
    }

    override fun onStart() {
        super.onStart()
        unusedCurrencies = preferences.allUnusedCurrencies
    }

    private fun updateCurrenciesList(query: String?) {
        val queryLowercase = query?.toLowerCase(Locale.ROOT)
        val newItems = unusedCurrencies.filter {
            it.matchesQuery(queryLowercase)
        }
        mAdapter.updateList(newItems)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.add_currency, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (mAdapter.itemCount == 1) {
                    recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                updateCurrenciesList(newText)
                return true
            }
        })
        searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
    }

    private fun closeKeyboard() {
        val view = requireView().findFocus()
        if (view != null) {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onItemClick(currency: Currency) {
        closeKeyboard()
        preferences.addToUserCurrencies(currency)
        singleActivityVM.addedCurrencyLD.postValue(currency)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}