package com.mediapocket.android.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.model.PodcastAdapterEntry
import com.mediapocket.android.viewmodels.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Vlad Namashko
 */
class GenrePodcastsFragment : SimplePodcastFragment() {

    override fun getTitle() = DependencyLocator.getInstance().context.getString(R.string.best_in_genre)

    override fun getLayoutId() = R.layout.subscribed_podcasts

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        arguments?.getInt(ARG_GENRE_ID)?.let {
            subscription.add(model.doLoadingAction { model.getTop(it, 100) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { res ->
                        activity?.title = res.title()
                        adapter.setItems(PodcastAdapterEntry.convert(res))
                        adapter.notifyDataSetChanged()
                    })
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val ARG_GENRE_ID = "arg_genre_id"

        fun newInstance(id: Int): GenrePodcastsFragment {
            val fragment =  GenrePodcastsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_GENRE_ID, id)
            }
            return fragment
        }

        const val TAG = "GenrePodcastsFragment"
    }
}