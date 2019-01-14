package com.tubitv.fragmentoperator.demo.fragment.container

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tubitv.fragmentoperator.demo.R
import com.tubitv.fragmentoperator.demo.databinding.FragmentContainerBinding
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragments.FragmentOperator

abstract class ContainerFragment : FoFragment() {

    companion object {
        private val TAG = ContainerFragment::class.simpleName
    }

    lateinit var mBinding: FragmentContainerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_container, container, false)

        // Prevent rebuild child backstack when resume container fragment
        if (childFragmentManager.fragments.count() == 0) { // When no visible fragment
            addInitialChildFragment(getInitialFragment(), R.id.child_fragment_container)
        }

        return mBinding.root
    }


    override fun onResume() {
        super.onResume()
        FragmentOperator.handlePendingChildFragments(this)
    }

    override fun getCurrentChildFragment(): FoFragment? {
        val fragment = getCurrentChildFragment(R.id.child_fragment_container)
        if (fragment != null && fragment is FoFragment) {
            return fragment
        }
        return null
    }

    fun showChildFragment(fragment: FoFragment,
                          clearStack: Boolean,
                          skipOnPop: Boolean) {
        showChildFragment(fragment, clearStack, skipOnPop, R.id.child_fragment_container)
    }

    override fun showChildFragment(fragment: FoFragment) {
        showChildFragment(fragment, false, false)
    }

    protected abstract fun getInitialFragment(): FoFragment
}