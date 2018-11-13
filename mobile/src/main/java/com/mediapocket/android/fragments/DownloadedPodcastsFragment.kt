package com.mediapocket.android.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * @author Vlad Namashko
 */
class DownloadedPodcastsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        Completable.fromAction {
//            val v = DependencyLocator.getInstance().database.downloadedPodcastItemDao().getAll()
//
//        }.subscribeOn(Schedulers.io()).subscribe()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.downloaded)

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false

    companion object {
        fun newInstance(): DownloadedPodcastsFragment {
            return DownloadedPodcastsFragment()
        }

        const val TAG = "DownloadedPodcastsFragment"
    }
}