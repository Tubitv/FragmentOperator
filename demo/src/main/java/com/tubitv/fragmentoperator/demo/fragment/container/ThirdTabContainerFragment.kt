package com.tubitv.fragmentoperator.demo.fragment.container

import com.tubitv.fragmentoperator.demo.fragment.child.ThirdTabFragment
import com.tubitv.fragmentoperator.fragment.FoFragment

class ThirdTabContainerFragment : ContainerFragment() {
    override fun getInitialFragment(): FoFragment {
        val fragment = ThirdTabFragment()
        return fragment
    }
}