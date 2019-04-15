package com.tubitv.fragmentoperator.fragment

import android.support.v4.app.FragmentManager
import com.tubitv.fragmentoperator.models.FoModels

/**
 * Wrapper class for FragmentManager so we can tag it
 */

class FragmentManagerHolder(fragmentManager: FragmentManager, tag: String) {
    companion object {
        private val TAG = FragmentManagerHolder::class.simpleName

        const val MAIN_ACTIVITY_FRAGMENT_MANAGER = "main_activity_fragment_manager"
    }

    var fragmentManager: FragmentManager? = fragmentManager
        private set

    var tag = tag
        private set

    /**
     * Custom handling for FragmentManager popBackStack
     * Clean up models every time pop back stack
     */
    fun handlePopBackStack(name: String?, flags: Int) {
        fragmentManager?.addOnBackStackChangedListener(mOnBackStackChangedListener)
        fragmentManager?.popBackStack(name, flags)
    }

    private val mOnBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        FoModels.cleanUpModels(this)
        fragmentManager?.removeOnBackStackChangedListener(getOnBackStackChangedListener())
    }

    private fun getOnBackStackChangedListener(): FragmentManager.OnBackStackChangedListener {
        return mOnBackStackChangedListener
    }
}