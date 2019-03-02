package com.mediapocket.android.fragments

import androidx.fragment.app.Fragment


/**
 * @author Vlad Namashko
 */
abstract class BaseFragment: androidx.fragment.app.Fragment() {

    abstract fun getTitle(): String

    abstract fun hasNavigation(): Boolean

    abstract fun hasBackNavigation(): Boolean

}