package com.tubitv.fragmentoperator.demo.fragment.child

import com.tubitv.fragmentoperator.demo.constant.THIRD_TAB_INDEX
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragments.FragmentOperator

@TabChildFragment(tabIndex = THIRD_TAB_INDEX)
class ThirdTabFragment : BaseTabFragment() {

    override fun getName(): String {
        return "Third Tab Child Fragment Index: $index"
    }

    override fun loadToCurrentTab() {
        val fragment = ThirdTabFragment()
        FragmentOperator.showFragment(fragment)
    }

    override fun loadToOtherTab() {
        val fragment = FirstTabFragment()
        FragmentOperator.showFragment(fragment)
    }
}