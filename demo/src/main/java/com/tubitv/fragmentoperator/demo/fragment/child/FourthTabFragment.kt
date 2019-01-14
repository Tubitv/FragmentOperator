package com.tubitv.fragmentoperator.demo.fragment.child

import com.tubitv.fragmentoperator.demo.constant.FOURTH_TAB_INDEX
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragments.FragmentOperator

@TabChildFragment(tabIndex = FOURTH_TAB_INDEX)
class FourthTabFragment : BaseTabFragment() {

    override fun getName(): String {
        return "Fourth Tab Child Fragment Index: $index"
    }

    override fun loadToCurrentTab() {
        val fragment = FourthTabFragment()
        FragmentOperator.showFragment(fragment)
    }

    override fun loadToOtherTab() {
        val fragment = FirstTabFragment()
        FragmentOperator.showFragment(fragment)
    }
}