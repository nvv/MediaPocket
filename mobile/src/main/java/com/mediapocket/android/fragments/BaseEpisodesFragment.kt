package com.mediapocket.android.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mediapocket.android.R
import com.mediapocket.android.extensions.getResourceIdAttribute
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.decoration.DividerItemDecoration
import com.mediapocket.android.viewmodels.EpisodesViewModel
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import javax.inject.Inject

/**
 * @author Vlad Namashko
 */
abstract class BaseEpisodesFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var model: EpisodesViewModel
    protected val subscription: CompositeDisposable = CompositeDisposable()

    protected lateinit var items: androidx.recyclerview.widget.RecyclerView
    protected lateinit var loading: ProgressBar

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this, viewModelFactory).get(EpisodesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = UI {
            frameLayout {
                backgroundResource = context.getResourceIdAttribute(R.attr.primaryBackgroundColor)
                lparams(width = matchParent, height = matchParent)

                items = recyclerView {

                }.lparams(width = matchParent, height = matchParent)

                loading = progressBar {
                    visibility = View.GONE
                }.lparams(width = wrapContent, height = wrapContent) {
                    gravity = Gravity.CENTER
                }
            }
        }.view

        subscription.add(model.loading().subscribe { isLoading ->
            items.visibility = if (isLoading) View.GONE else View.VISIBLE
            loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        items.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        items.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL_LIST).setPadding(ViewUtils.getDimensionSize(16)))

        return view
    }

    override fun onDetach() {
        super.onDetach()
        subscription.dispose()
    }

}