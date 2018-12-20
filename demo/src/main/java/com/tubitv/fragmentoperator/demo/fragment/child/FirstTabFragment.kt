package com.tubitv.fragmentoperator.demo.fragment.child

import com.tubitv.fragmentoperator.demo.constant.FIRST_TAB_INDEX
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragments.FragmentOperator

@TabChildFragment(tabIndex = FIRST_TAB_INDEX)
class FirstTabFragment : BaseTabFragment() {


    override fun getName(): String {
        return "First Tab Child Fragment Index: $index"
    }

    override fun loadToCurrentTab() {
        val fragment = FirstTabFragment()
        FragmentOperator.showFragment(fragment)
    }

    override fun loadToOtherTab() {
        val fragment = ThirdTabFragment()
        FragmentOperator.showFragment(fragment)
    }
}