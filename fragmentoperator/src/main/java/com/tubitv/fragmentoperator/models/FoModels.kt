package com.tubitv.fragmentoperator.models

import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.fragment.FragmentManagerHolder
import com.tubitv.fragmentoperator.logging.FoLog

/**
 * Singleton model class to maintain all models for fragments
 *
 * Fragments models are saved per host FragmentManager in a map of FragmentManagerModels
 *
 * Structure (3 layers HashMap):
 *
 * Host FragmentManager Tag -> Fragment Tag -> Key (String) -> Model (Any)
 *                      (HashMap)       (HashMap)        (HashMap)
 */

object FoModels {

    private val TAG = FoModels::class.simpleName

    private val mFragmentManagerModels: HashMap<String, FragmentManagerModels> = hashMapOf()

    /**
     * Add all models for a specific fragment instance, which is saved separately for each host FragmentManager
     *
     * @param fragment                  Fragment instance
     * @param modelMap                  Map of all models for the fragment instance
     */
    fun add(fragment: FoFragment, modelMap: HashMap<String, Any>) {
        val tagForFragmentManager = fragment.getHostFragmentManagerTag()

        if (tagForFragmentManager != null) {

            if (mFragmentManagerModels.containsKey(tagForFragmentManager)) {
                mFragmentManagerModels[tagForFragmentManager]?.add(fragment, modelMap)
            } else {
                val fragmentManagerModels = FragmentManagerModels()
                fragmentManagerModels.add(fragment, modelMap)
                mFragmentManagerModels[tagForFragmentManager] = fragmentManagerModels
            }
        }
    }

    /**
     * Use key to look up model for fragment instance
     *
     * @param fragment  Fragment instance
     * @param key       String key to reference model
     */
    fun <T : Any> get(fragment: FoFragment, key: String): T? {
        val fragmentManagerTag = fragment.getHostFragmentManagerTag()

        if (fragmentManagerTag != null) {
            return mFragmentManagerModels[fragmentManagerTag]?.get(fragment, key)
        }

        return null
    }

    /**
     * Clean up models for the host FragmentManager
     *
     * @param fragmentManagerHolder  Host FragmentManager wrapper
     */
    fun cleanUpModels(fragmentManagerHolder: FragmentManagerHolder) {
        FoLog.d(TAG, "cleanUpModels for FragmentManager with tag: " + fragmentManagerHolder.tag)

        val fragmentManager = fragmentManagerHolder.fragmentManager
        if (fragmentManager != null) {
            mFragmentManagerModels[fragmentManagerHolder.tag]?.cleanUp(fragmentManager)
        }
    }
}