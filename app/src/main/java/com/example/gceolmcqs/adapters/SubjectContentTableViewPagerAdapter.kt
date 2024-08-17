package com.example.gceolmcqs.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SubjectContentTableViewPagerAdapter(
    private val supportFragmentManager: FragmentManager,
    private val tabFragments: ArrayList<Fragment>,
    private val tabTitles: ArrayList<String?>
) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    override fun getCount(): Int {
        return tabFragments.size
    }

    override fun getItem(position: Int): Fragment {
        return tabFragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}