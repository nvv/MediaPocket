package com.mediapocket.android.fragments

import android.support.v4.app.Fragment


/**
 * @author Vlad Namashko
 */
abstract class BaseFragment: Fragment() {

    abstract fun getTitle(): String

    abstract fun hasNavigation(): Boolean

    abstract fun hasBackNavigation(): Boolean

}