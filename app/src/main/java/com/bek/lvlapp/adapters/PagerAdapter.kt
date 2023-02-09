package com.bek.lvlapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(fm: FragmentManager?) :
    FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val pFragmentList = ArrayList<Fragment>()
    private val pFragmentTitle = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return pFragmentList[position]
    }

    override fun getCount(): Int {
        return pFragmentList.size
    }

    fun addFragment(fm: Fragment, title: String){
        pFragmentList.add(fm)
        pFragmentTitle.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pFragmentTitle.get(position)
    }
}