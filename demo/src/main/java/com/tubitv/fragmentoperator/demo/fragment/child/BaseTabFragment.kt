package com.tubitv.fragmentoperator.demo.fragment.child

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tubitv.fragmentoperator.demo.R
import com.tubitv.fragmentoperator.demo.constant.ANY_TAB_INDEX
import com.tubitv.fragmentoperator.demo.databinding.FragmentTabChildBinding
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragments.FragmentOperator

@TabChildFragment(tabIndex = ANY_TAB_INDEX)
open class BaseTabFragment : FoFragment() {

    companion object {
        private val TAG = BaseTabFragment::class.simpleName
        private const val NAME = "name"
        private const val DEFAULT_NAME = "TabChildFragment"

        @JvmStatic
        fun newInstance(name: String): BaseTabFragment {
            val fragment = BaseTabFragment()
            val bundle = Bundle()
            bundle.putString(NAME, name)
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var mBinding: FragmentTabChildBinding
    private var mName: String = DEFAULT_NAME
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mName = arguments?.getString(NAME, DEFAULT_NAME) ?: DEFAULT_NAME
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_child, container, false)

        // update index based on current container backstack
        val containerFragment = FragmentOperator.getTabsNavigator()?.getCurrentContainerFragment()
        if (containerFragment != null) {
            index = containerFragment.childFragmentManager.backStackEntryCount
        }

        mBinding.nameTextView.text = getName()

        mBinding.loadToCurrentButton.setOnClickListener { _ ->
            loadToCurrentTab()
        }

        mBinding.loadToOtherButton.setOnClickListener { _ ->
            loadToOtherTab()
        }

        return mBinding.root
    }

    open fun getName(): String {
        return mName
    }

    open fun loadToCurrentTab() {}

    open fun loadToOtherTab() {}
}