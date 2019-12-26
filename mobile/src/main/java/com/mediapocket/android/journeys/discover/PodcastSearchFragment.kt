package com.mediapocket.android.journeys.discover

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mediapocket.android.R
import com.mediapocket.android.fragments.BasePodcastFragment
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.journeys.discover.vm.SearchPodcastViewModel


/**
 * @author Vlad Namashko
 */
class PodcastSearchFragment : BasePodcastFragment() {

    private lateinit var model: SearchPodcastViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this, viewModelFactory).get(SearchPodcastViewModel::class.java)

        model.searchResult.observe(this, Observer {
            adapter.setItems(PodcastAdapterEntry.convert(it))
            adapter.notifyDataSetChanged()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (arguments?.getString(ARG_QUERY) != null) {
            searchPodcast(arguments?.getString(ARG_QUERY))
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun isLoading() = model.isLoading

    override fun getLayoutId(): Int {
        return R.layout.search_podcast
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)

        val search : SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        search.isIconified = false

        search.maxWidth = Integer.MAX_VALUE

        if (arguments?.getString(ARG_QUERY) != null) {
            search.setQuery(arguments?.getString(ARG_QUERY), true)
        }

        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchPodcast(query)
                hideKeyboard()

                if (arguments == null) {
                    arguments = Bundle()
                }
                arguments?.putString(ARG_QUERY, query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        search.setOnCloseListener {
            activity?.supportFragmentManager?.popBackStack()
            false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun searchPodcast(query: String?) {
        query?.let {
            model.search(query)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun getTitle(): String? = null

    override fun hasNavigation(): Boolean = true

    override fun hasBackNavigation(): Boolean = true

    companion object {
        private val ARG_QUERY = "ARG_QUERY"

        const val TAG = "PodcastSearchFragment"

        fun newInstance(): PodcastSearchFragment {
            return PodcastSearchFragment()
        }
    }
}