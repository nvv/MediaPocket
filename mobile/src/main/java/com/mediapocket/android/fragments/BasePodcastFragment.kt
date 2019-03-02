package com.mediapocket.android.fragments

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.mediapocket.android.R
import com.mediapocket.android.adapters.PodcastGridAdapter
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.SpaceItemDecoration
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import org.reactivestreams.Subscription

/**
 * @author Vlad Namashko
 */
abstract class BasePodcastFragment : BaseFragment() {

    protected val adapter: PodcastGridAdapter = PodcastGridAdapter()
    protected val subscription: CompositeDisposable = CompositeDisposable()

    protected var loading: ProgressBar? = null
    protected var podcasts: androidx.recyclerview.widget.RecyclerView? = null

    protected var animatedAdapter: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        podcasts = view?.findViewById(R.id.items)
        loading = view?.findViewById(R.id.loading)
        podcasts?.addItemDecoration(SpaceItemDecoration(PodcastGridAdapter.ITEM_GAP))

        adapter.setItemsInRowCount(calculateItemsCount())
        podcasts?.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, calculateItemsCount())

        subscription.add(loading().subscribe { isLoading -> syncVisibility(isLoading) })

        podcasts?.adapter = if (animatedAdapter) ScaleInAnimationAdapter(adapter, 0.9f) else adapter
        animatedAdapter = false
        return view
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        adapter.setItemsInRowCount(calculateItemsCount())
        podcasts?.layoutManager = androidx.recyclerview.widget.GridLayoutManager(view?.context, calculateItemsCount())
        adapter.notifyDataSetChanged()
    }

    private fun syncVisibility(isLoading: Boolean) {
        podcasts?.visibility = if (isLoading) View.GONE else View.VISIBLE
        loading?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    protected fun calculateItemsCount() =
            if (ViewUtils.isTablet())
                if (activity?.resources?.configuration?.orientation == ORIENTATION_PORTRAIT) 3 else 4
            else 2

    protected abstract fun loading(): BehaviorSubject<Boolean>

    protected abstract fun getLayoutId(): Int
}