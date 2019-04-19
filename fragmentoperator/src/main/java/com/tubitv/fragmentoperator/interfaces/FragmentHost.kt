package com.tubitv.fragmentoperator.interfaces

import android.support.v4.app.FragmentManager
import com.tubitv.fragmentoperator.logging.FoLog
import com.tubitv.fragmentoperator.models.FoModels

interface FragmentHost {

    companion object {
        private val TAG = FragmentHost::class.simpleName
    }

    private val mOnBackStackChangedListener: FragmentManager.OnBackStackChangedListener
        get() = FragmentManager.OnBackStackChangedListener {
            FoModels.cleanUpModels(this)
            FoLog.d(TAG, "about to remove OnBackStackChangedListener")
            getHostFragmentManager().removeOnBackStackChangedListener(getOnBackStackChangedListener())
        }

    fun getHostFragmentManager(): FragmentManager

    fun getFragmentManagerTag(): String

    fun isReadyForFragmentOperation(): Boolean

    /**
     * Custom handling for FragmentManager popBackStack
     * Clean up models every time pop back stack
     */
    fun handlePopBackStack(name: String?, flags: Int) {
        getHostFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener)
        getHostFragmentManager().popBackStack(name, flags)
    }

    private fun getOnBackStackChangedListener(): FragmentManager.OnBackStackChangedListener {
        return mOnBackStackChangedListener
    }
}