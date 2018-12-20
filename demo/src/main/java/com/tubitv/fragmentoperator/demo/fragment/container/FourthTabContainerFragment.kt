package com.tubitv.fragmentoperator.demo.fragment.container

import com.tubitv.fragmentoperator.demo.fragment.child.FourthTabFragment
import com.tubitv.fragmentoperator.fragment.FoFragment

class FourthTabContainerFragment : ContainerFragment() {
    override fun getInitialFragment(): FoFragment {
        val fragment = FourthTabFragment()
        return fragment
    }
}