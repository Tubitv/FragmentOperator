package com.tubitv.fragmentoperator.`interface`

import com.tubitv.fragmentoperator.fragment.FoFragment

interface TabsNavigator {

    fun getCurrentTabIndex(): Int

    fun getCurrentContainerFragment(): FoFragment?

    fun isTabIndexValid(tabIndex: Int): Boolean
    
    fun switchToTab(index: Int)
}