package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.viewmodels.PodcastViewModel
import com.mediapocket.android.viewmodels.SearchPodcastViewModel
import io.reactivex.subjects.BehaviorSubject


/**
 * @author Vlad Namashko
 */
class PodcastSearchFragment : BasePodcastFragment() {

    private val ARG_QUERY = "ARG_QUERY"

    private lateinit var model: SearchPodcastViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this).get(SearchPodcastViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (arguments?.getString(ARG_QUERY) != null) {
            searchPodcast(arguments?.getString(ARG_QUERY))
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun getLayoutId(): Int {
        return R.layout.search_podcast
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.search_menu, menu)

        val search : SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        search.isIconified = false

        val searchEditFrame = search.findViewById<LinearLayout>(R.id.search_edit_frame)
        (searchEditFrame.layoutParams as LinearLayout.LayoutParams).leftMargin = -24

        if (arguments?.getString(ARG_QUERY) != null) {
            search.setQuery(arguments?.getString(ARG_QUERY), true)
        }

//        search.setOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus) {
//                search.setQuery(arguments?.getString(ARG_QUERY), true)
//            }
//        }

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun searchPodcast(query: String?) {
        subscription.add(model.search(query!!).subscribe { res ->
            adapter.setItems(PodcastAdapterEntry.convert(res))
            adapter.notifyDataSetChanged()
        })
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDetach() {
        super.onDetach()
        subscription.dispose()
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.title_search)

    override fun hasNavigation(): Boolean = true

    override fun hasBackNavigation(): Boolean = true

    override fun loading() = model.loading()

    companion object {
        fun newInstance(): PodcastSearchFragment {
            return PodcastSearchFragment()
        }

        const val TAG = "PodcastSearchFragment"
    }
}