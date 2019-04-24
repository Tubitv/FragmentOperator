package com.tubitv.fragmentoperator.models

import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.logging.FoLog

/**
 * Models class for each host FragmentManager
 */
class FragmentManagerModels {
    companion object {
        private val TAG = FragmentManagerModels::class.simpleName
    }

    private val mModels: HashMap<String, HashMap<String, Any>> = hashMapOf()

    /**
     * Add model for the fragment instance
     *
     * @param fragment  Fragment instance
     * @param key       Key to reference model
     * @param model     Model to be saved for fragment
     */
    fun add(fragment: FoFragment, key: String, model: Any) {
        val tag = fragment.getFragmentTag()

        val fragmentModelMap = mModels[tag]
        if (fragmentModelMap != null) {
            fragmentModelMap[key] = model
        } else {
            val newMap: HashMap<String, Any> = hashMapOf()
            newMap[key] = model
            mModels[tag] = newMap
        }
    }

    /**
     * Add all models of the fragment instance
     *
     * @param fragment  Fragment instance
     * @param modelMap  Map of all models of the fragment instance
     */
    fun add(fragment: FoFragment, modelMap: HashMap<String, Any>) {
        val tag = fragment.getFragmentTag()

        val fragmentModelMap = mModels[tag]
        if (fragmentModelMap != null) {
            fragmentModelMap.putAll(modelMap)
        } else {
            mModels[tag] = modelMap
        }
    }

    /**
     * Use key to look up model for fragment instance
     *
     * @param fragment  Fragment instance
     * @param key       String key to reference model
     */
    fun <T : Any> get(fragment: FoFragment, key: String): T? {
        val tag = fragment.getFragmentTag()

        if (tag != null) {
            return mModels[tag]?.get(key) as? T
        }
        return null
    }

    /**
     * Use key to look up model for fragment instance
     *
     * @param fragmentTag   Tag for fragment instance
     * @param key           String key to reference model
     */
    fun <T : Any> get(fragmentTag: String, key: String): T? {
        return mModels[fragmentTag]?.get(key) as? T
    }


    /**
     * Clean up models for the host FragmentManager
     *
     * This will loop through all fragments in backstack and discard any models that are not needed any more
     *
     * @param fragmentManager  Host FragmentManager
     */
    fun cleanUp(fragmentManager: FragmentManager) {
        FoLog.d(TAG, "Before cleanUp models map size: " + mModels.size)

        // If models is empty, no need to loop through fragments
        if (mModels.isEmpty()) {
            return
        }

        val updateMap: HashMap<String, HashMap<String, Any>> = hashMapOf()

        // Check visible fragments
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (fragment in fragmentManager.fragments) {
                copyDataForFragment(updateMap, fragment)
            }
        }

        // Check fragments in back stack
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val fragment = fragmentManager
                    .findFragmentByTag(fragmentManager.getBackStackEntryAt(i).name)
            copyDataForFragment(updateMap, fragment)
        }

        mModels.clear() // clear all reference
        mModels.putAll(updateMap) // only keep models that are still needed
        FoLog.d(TAG, "After cleanUp models map size: " + mModels.size)
    }

    private fun copyDataForFragment(updateMap: HashMap<String, HashMap<String, Any>>, fragment: Fragment?) {
        (fragment as? FoFragment)?.let { foFragment ->
            foFragment.getFragmentTag()?.let { tag ->
                mModels[tag]?.let { data ->
                    updateMap[tag] = data
                }
            }
        }
    }
}