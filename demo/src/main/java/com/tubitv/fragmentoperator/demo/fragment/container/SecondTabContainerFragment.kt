package com.tubitv.fragmentoperator.demo.fragment.container

import com.tubitv.fragmentoperator.demo.fragment.child.SecondTabFragment
import com.tubitv.fragmentoperator.fragment.FoFragment

class SecondTabContainerFragment : ContainerFragment() {
    override fun getInitialFragment(): FoFragment {
        val fragment = SecondTabFragment()
        return fragment
    }
}