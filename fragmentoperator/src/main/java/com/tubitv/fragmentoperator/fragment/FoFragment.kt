package com.tubitv.fragmentoperator.fragment

import android.os.Bundle
import android.support.v4.app.Fragment

open class FoFragment : Fragment() {
    private val TAG = FoFragment::class.simpleName

    private val PREVIOUS_FRAGMENT_TAG = "previous_fragment_tag"
    private val CURRENT_FRAGMENT_TAG = "current_fragment_tag"
    private val FRAGMENT_TAG_SEPERATOR = ":"

    var skipOnPop = false // Flag to mark if current instance should be skipped when pop back stack
    var previousFragmentTag: String? = null // Tag for fragment that will go to when pop back from current instance

    private var mCurrentFragmentTag: String? = null // Tag for current fragment instance

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save previous fragment tag and current fragment tag, so we don't lose them during fragment recreation
        outState.putString(PREVIOUS_FRAGMENT_TAG, previousFragmentTag)
        outState.putString(CURRENT_FRAGMENT_TAG, getFragmentTag())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            previousFragmentTag = savedInstanceState.getString(PREVIOUS_FRAGMENT_TAG)
            mCurrentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)
        }
    }

    /**
     * Return the unique tag per fragment instance
     * Since fragment can be re-created when activity configuration changed (i.e. screen rotation) and hashCode will
     * change as well, we should only and always use the initial hashCode.
     *
     * @return Unique tag string per instance
     */
    fun getFragmentTag(): String? {
        if (mCurrentFragmentTag == null) {
            mCurrentFragmentTag = this.javaClass.simpleName + FRAGMENT_TAG_SEPERATOR + this.hashCode()
        }
        return mCurrentFragmentTag
    }
}