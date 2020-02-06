package com.mediapocket.android.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mediapocket.android.R
import com.mediapocket.android.core.DependencyLocator
import androidx.fragment.app.FragmentManager
import com.mediapocket.android.fragments.BaseFragment
import kotlinx.android.synthetic.main.episodes_view.*


/**
 * @author Vlad Namashko
 */
class EpisodesFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.episodes_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // attach tablayout with viewpager
        tablayout.setupWithViewPager(viewpager)

        val adapter = EpisodesPagerAdapter(childFragmentManager)

        adapter.addFrag(DownloadedEpisodesFragment.newInstance(), getString(R.string.downloaded))
        adapter.addFrag(FavouritesEpisodesFragment.newInstance(), getString(R.string.favourites))

        viewpager.adapter = adapter
    }

    override fun getTitle(): String = DependencyLocator.getInstance().context.getString(R.string.episodes)

    override fun hasNavigation() = true

    override fun hasBackNavigation() = false

    companion object {
        fun newInstance(): EpisodesFragment {
            return EpisodesFragment()
        }

        const val TAG = "EpisodesFragment"
    }

    class EpisodesPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFrag(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }
}