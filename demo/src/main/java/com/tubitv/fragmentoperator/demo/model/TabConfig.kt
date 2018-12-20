package com.tubitv.views.viewmodels

class TabConfig(val fragmentClass: Class<*>,
                val iconResId: Int,
                val titleStringId: Int) {

    fun getTag(): String {
        return fragmentClass.simpleName // We use class name as child fragment tag for tabs
    }
}