package com.tubitv.views.viewmodels


class TabsViewModel {

    private val mTabConfigList: MutableList<TabConfig> = mutableListOf()
    private val mTabClassMap: MutableMap<Class<*>, Int> = mutableMapOf()

    fun addTabConfig(tabConfig: TabConfig) {
        mTabConfigList.add(tabConfig)
        mTabClassMap[tabConfig.fragmentClass] = mTabConfigList.size - 1 // current index in list
    }

    fun getTabConfigList(): List<TabConfig> {
        return mTabConfigList
    }

    fun getTagForTabIndex(index: Int): String {
        if (index < 0 || index > mTabConfigList.size - 1) { // Check if index is valid
            return ""
        }
        return mTabConfigList[index].getTag()
    }

    fun size(): Int {
        return mTabConfigList.size
    }

    fun clear() {
        mTabConfigList.clear()
        mTabClassMap.clear()
    }
}