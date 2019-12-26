package com.mediapocket.android.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


/**
 * @author Vlad Namashko
 */
abstract class BaseFragment: Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    abstract fun getTitle(): String?

    abstract fun hasNavigation(): Boolean

    abstract fun hasBackNavigation(): Boolean

}