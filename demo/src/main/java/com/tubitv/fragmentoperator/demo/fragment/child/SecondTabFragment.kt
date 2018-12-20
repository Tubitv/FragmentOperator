package com.tubitv.fragmentoperator.demo.fragment.child

import com.tubitv.fragmentoperator.demo.constant.SECOND_TAB_INDEX
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragments.FragmentOperator


@TabChildFragment(tabIndex = SECOND_TAB_INDEX)
class SecondTabFragment : BaseTabFragment() {


    override fun getName(): String {
        return "Second Tab Child Fragment Index: $index"
    }

    override fun loadToCurrentTab() {
        val fragment = SecondTabFragment()
        FragmentOperator.showFragment(fragment)
    }

    override fun loadToOtherTab() {
        val fragment = ThirdTabFragment()
        FragmentOperator.showFragment(fragment)
    }
}