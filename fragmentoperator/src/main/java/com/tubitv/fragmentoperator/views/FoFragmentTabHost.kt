package com.tubitv.fragmentoperator.views

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentTabHost
import android.util.AttributeSet
import com.tubitv.fragmentoperator.logging.FoLog


/**
 * This class overrides FragmentTabHost onAttachedToWindow to handle crashes caused by
 * fragment commit operation after host onSaveInstance
 */
class FoFragmentTabHost(context: Context, attrs: AttributeSet) : FragmentTabHost(context, attrs) {

    companion object {
        private val TAG = FoFragmentTabHost::class.simpleName
    }

    override fun onAttachedToWindow() {
        try {
            super.onAttachedToWindow()
        } catch (e: IllegalStateException) {
            // This is to fix crash on checkStateLoss when FragmentTabHost tries to load fragments after onSaveInstance
            FoLog.w(TAG, e)
            (context as? Activity)?.let { activity ->
                activity.recreate()
            }
        }

    }
}