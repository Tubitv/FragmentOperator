package com.tubitv.fragmentoperator.demo.fragment

import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TabWidget
import android.widget.TextView
import com.tubitv.fragmentoperator.demo.R
import com.tubitv.fragmentoperator.demo.databinding.FragmentMainBinding
import com.tubitv.fragmentoperator.demo.databinding.TabItemBinding
import com.tubitv.fragmentoperator.demo.fragment.container.FirstTabContainerFragment
import com.tubitv.fragmentoperator.demo.fragment.container.FourthTabContainerFragment
import com.tubitv.fragmentoperator.demo.fragment.container.SecondTabContainerFragment
import com.tubitv.fragmentoperator.demo.fragment.container.ThirdTabContainerFragment
import com.tubitv.fragmentoperator.demo.model.TabConfig
import com.tubitv.fragmentoperator.demo.model.TabsViewModel
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.interfaces.TabsNavigator
import com.tubitv.fragments.FragmentOperator

class MainFragment : FoFragment(), TabsNavigator {

    companion object {
        private val TAG = MainFragment::class.simpleName
    }

    private lateinit var mBinding: FragmentMainBinding
    private val mTabsViewModel = TabsViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        // Setup tabhost
        mBinding.tabhost.setup(activity, childFragmentManager, android.R.id.tabcontent)
        mBinding.tabhost.tabWidget.showDividers = TabWidget.SHOW_DIVIDER_NONE

        // Setup models to populate tabs
        setupTabsViewModel()

        // Populate tabs
        populateTabs(inflater)

        // Update color for each tab
        updateTabView()

        // Re-populate tab color every time switching tab
        mBinding.tabhost.setOnTabChangedListener { updateTabView() }

        // Set up tab tap listener to handle pop back to root child fragment
        setupTabListeners()

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        FragmentOperator.registerTabsNavigator(this)
    }

    override fun onPause() {
        super.onPause()
        FragmentOperator.unregisterTabsNavigator()
    }

    override fun getCurrentTabIndex(): Int {
        return mBinding.tabhost.currentTab
    }

    override fun getCurrentContainerFragment(): FoFragment? {
        val tag = mTabsViewModel.getTagForTabIndex(mBinding.tabhost.currentTab)
        val containerFragment = childFragmentManager.findFragmentByTag(tag)

        if (containerFragment != null && containerFragment is FoFragment) {
            return containerFragment
        }
        return null
    }

    override fun isTabIndexValid(tabIndex: Int): Boolean {
        return tabIndex > -1 && tabIndex < mTabsViewModel.size() // Check if tabIndex starts from 0 to last index
    }


    override fun switchToTab(index: Int) {
        if (!isTabIndexValid(index)) {
            return
        }

        mBinding.tabhost.currentTab = index
    }

    private fun setupTabsViewModel() {
        mTabsViewModel.clear()

        mTabsViewModel.addTabConfig(TabConfig(FirstTabContainerFragment::class.java, android.R.drawable.ic_menu_help, R.string.tab_1_name))
        mTabsViewModel.addTabConfig(TabConfig(SecondTabContainerFragment::class.java, android.R.drawable.ic_menu_add, R.string.tab_2_name))
        mTabsViewModel.addTabConfig(TabConfig(ThirdTabContainerFragment::class.java, android.R.drawable.ic_menu_call, R.string.tab_3_name))
        mTabsViewModel.addTabConfig(TabConfig(FourthTabContainerFragment::class.java, android.R.drawable.ic_menu_directions, R.string.tab_4_name))
    }

    private fun populateTabs(inflater: LayoutInflater) {
        for (tabConfig in mTabsViewModel.getTabConfigList()) {
            val tabView = getTabItemView(inflater, tabConfig)
            mBinding.tabhost.addTab(
                    mBinding.tabhost.newTabSpec(tabConfig.getTag()).setIndicator(tabView),
                    tabConfig.fragmentClass,
                    null)
        }
    }

    private fun getTabItemView(inflater: LayoutInflater, tabConfig: TabConfig): View {
        val itemViewBinding: TabItemBinding = DataBindingUtil.inflate(inflater, R.layout.tab_item, null, false)
        itemViewBinding.iconImageView.setImageResource(tabConfig.iconResId)
        itemViewBinding.titleTextView.text = getString(tabConfig.titleStringId)
        return itemViewBinding.root
    }

    private fun updateTabView() {
        for (index in 0 until mBinding.tabhost.tabWidget.childCount) {
            val colorId =
                    if (index == mBinding.tabhost.currentTab) android.R.color.holo_orange_dark else android.R.color.darker_gray

            val iconImageView: ImageView =
                    mBinding.tabhost.tabWidget.getChildAt(index).findViewById(R.id.icon_image_view)
            val titleTextView: TextView =
                    mBinding.tabhost.tabWidget.getChildAt(index).findViewById(R.id.title_text_view)

            val context = context
            if (context != null) {
                val color = ContextCompat.getColor(context, colorId)

                iconImageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                titleTextView.setTextColor(color)
            }
        }
    }

    private fun setupTabListeners() {
        for (index in 0 until mBinding.tabhost.tabWidget.childCount) {
            mBinding.tabhost.tabWidget.getChildAt(index).setOnClickListener { view ->
                if (index == mBinding.tabhost.currentTab) {
                    FragmentOperator.handleTabPopToRootFragment()
                } else {
                    mBinding.tabhost.currentTab = index
                }
            }
        }
    }
}
