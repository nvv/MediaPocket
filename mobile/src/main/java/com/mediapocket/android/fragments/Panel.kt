package com.mediapocket.android.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mediapocket.android.R

/**
 * @author Vlad Namashko
 */
class Panel: BaseFragment() {

    private lateinit var mMessage: String

    companion object {
        fun newInstance(message: String): Panel {
            val panel = Panel()
            panel.mMessage = message
            return panel
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.panel, container, false)
        view?.findViewById<TextView>(R.id.message)?.text = mMessage
        return view
    }

    override fun getTitle() = ""

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false
}