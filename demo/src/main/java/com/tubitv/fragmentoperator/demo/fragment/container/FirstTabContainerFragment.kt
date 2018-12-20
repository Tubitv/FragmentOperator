package com.tubitv.fragmentoperator.demo.fragment.container

import com.tubitv.fragmentoperator.demo.fragment.child.FirstTabFragment
import com.tubitv.fragmentoperator.fragment.FoFragment

class FirstTabContainerFragment : ContainerFragment() {
    override fun getInitialFragment(): FoFragment {
        val fragment = FirstTabFragment()
        return fragment
    }
}