package com.tubitv.fragmentoperator.fragment

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.tubitv.fragmentoperator.interfaces.FragmentHost
import com.tubitv.fragmentoperator.models.FoModels
import com.tubitv.fragments.FragmentOperator

open class FoFragment : Fragment(), FragmentHost {
    companion object {
        private val TAG = FoFragment::class.simpleName

        private const val PREVIOUS_FRAGMENT_TAG = "previous_fragment_tag"
        private const val CURRENT_FRAGMENT_TAG = "current_fragment_tag"
        private const val ROOT_CHILD_FRAGMENT_TAG = "root_child_fragment_tag"
        private const val HOST_FRAGMENT_MANAGER_TAG = "host_fragment_manager_tag"
        private const val FRAGMENT_TAG_SEPERATOR = ":"
    }

    var skipOnPop = false // Flag to mark if current instance should be skipped when pop back stack
    var previousFragmentTag: String? = null // Tag for fragment that will go to when pop back from current instance

    private var mCurrentFragmentTag: String? = null // Tag for current fragment instance
    private var mRootChildFragmentTag: String? = null // Tag for root child fragment instance

    private var mHostFragmentManagerTag: String? = null // Tag for which fragment manager is this instance loaded to

    private var mChildFragmentManagerPrepared: Boolean = false

    private val mArguments: HashMap<String, Any> = hashMapOf()

    /**
     * Provide a method to show fragment directly without specific container config
     *
     * @param fragment    Fragment Instance
     */
    open fun showChildFragment(fragment: FoFragment) {
        // Implement if needed
    }

    /**
     * Provide a method to directly get current child fragment
     *
     * @return Current child fragment Instance
     */
    open fun getCurrentChildFragment(): FoFragment? {
        return null
    }

    /**
     * Provide a method to let fragment handle back button pressed
     *
     * @return True if fragment has handled back button pressed
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    /**
     * Provide a method to let fragment handle bottom tab widget pressed
     *
     * @return True if fragment has handled bottom tab widget pressed
     */
    open fun onTabClicked(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            previousFragmentTag = savedInstanceState.getString(PREVIOUS_FRAGMENT_TAG)
            mCurrentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)
            mRootChildFragmentTag = savedInstanceState.getString(ROOT_CHILD_FRAGMENT_TAG)
            mHostFragmentManagerTag = savedInstanceState.getString(HOST_FRAGMENT_MANAGER_TAG)
        }
        mChildFragmentManagerPrepared = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mChildFragmentManagerPrepared = false

        super.onSaveInstanceState(outState)

        // Save previous fragment tag and current fragment tag, so we don't lose them during fragment recreation
        outState.putString(PREVIOUS_FRAGMENT_TAG, previousFragmentTag)
        outState.putString(CURRENT_FRAGMENT_TAG, getFragmentTag())
        outState.putString(ROOT_CHILD_FRAGMENT_TAG, mRootChildFragmentTag)
        outState.putString(HOST_FRAGMENT_MANAGER_TAG, mHostFragmentManagerTag)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            previousFragmentTag = savedInstanceState.getString(PREVIOUS_FRAGMENT_TAG)
            mCurrentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)
            mRootChildFragmentTag = savedInstanceState.getString(ROOT_CHILD_FRAGMENT_TAG)
        }
        mChildFragmentManagerPrepared = true
    }

    override fun onResume() {
        super.onResume()
        mChildFragmentManagerPrepared = true
    }

    override fun onPause() {
        super.onPause()
        mChildFragmentManagerPrepared = false
    }

    override fun isReadyForFragmentOperation(): Boolean {
        return isAdded && mChildFragmentManagerPrepared
    }

    override fun getFragmentManagerTag(): String {
        return getFragmentTag()
    }

    override fun getHostFragmentManager(): FragmentManager {
        return childFragmentManager
    }

    /**
     * Return the unique tag per fragment instance
     * Since fragment can be re-created when activity configuration changed (i.e. screen rotation) and hashCode will
     * change as well, we should only and always use the initial hashCode.
     *
     * @return Unique tag string per instance
     */
    fun getFragmentTag(): String {
        val tag = mCurrentFragmentTag

        return if (tag == null) {
            val newTag = this.javaClass.simpleName + FRAGMENT_TAG_SEPERATOR + this.hashCode()
            mCurrentFragmentTag = newTag
            newTag
        } else {
            tag
        }
    }

    /**
     * Return the tag for host FragmentManager
     *
     * @return Unique tag string for host FragmentManager
     */
    fun getHostFragmentManagerTag(): String? {
        return mHostFragmentManagerTag
    }

    /**
     * Get the tag of root child fragment instance if any
     *
     * @return Root child fragment Tag, null if there is no child fragment
     */
    fun getRootChildFragmentTag(): String? {
        return mRootChildFragmentTag
    }

    /**
     * Add a initial child fragment.
     *
     * @param fragment    Fragment Instance
     * @param containerId Fragment Container ID
     */
    fun addInitialChildFragment(fragment: FoFragment, containerId: Int) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment, fragment.getFragmentTag())
        fragmentTransaction.addToBackStack(fragment.getFragmentTag())

        // Save tag to track which FragmentManager the fragment belongs to
        fragment.addHostFragmentManagerTag(getFragmentTag())

        // Save models so we can recover when fragments get recreated
        FoModels.add(fragment, fragment.getAllArguments())

        fragmentTransaction.commit()
        mRootChildFragmentTag = fragment.getFragmentTag()
    }

    /**
     * Add host FragmentManager tag
     *
     * @param tag   Tag for host FragmentManager
     */
    fun addHostFragmentManagerTag(tag: String) {
        mHostFragmentManagerTag = tag
    }

    /**
     * Show a fragment in a container with control of clearing previous back stack and setting fragment to be skipped on back
     *
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     * @param skipOnPop Flag for fragment to be skipped when pop back stack
     * @param containerId Container resource ID to display the fragment
     */
    fun showChildFragment(fragment: FoFragment,
                          clearStack: Boolean,
                          skipOnPop: Boolean,
                          @IdRes containerId: Int) {
        FragmentOperator.showFragment(this, fragment, clearStack, skipOnPop, containerId)
    }

    fun getCurrentChildFragment(@IdRes containerId: Int): FoFragment? {
        return FragmentOperator.getCurrentFragment(childFragmentManager, containerId)
    }

    /**
     * Set in memory model which is persistent through fragment recreation
     *
     * @param key   Key to reference model
     * @param data  Model to be set
     */
    fun addArgument(key: String, data: Any) {
        mArguments[key] = data
    }

    fun getAllArguments(): HashMap<String, Any> {
        return mArguments
    }

    /**
     * Add model which is persistent during fragment recreation
     * This will not do anything if fragment instance hasn't been loaded
     *
     * @param key   Key to reference model
     * @param model  Model to be add
     */
    fun addModelIfFragmentLoaded(key: String, model: Any) {
        FoModels.add(this, key, model)
    }

    /**
     * Get in memory model which is persistent through fragment recreation
     *
     * @param key   Key to reference model
     */
    fun <T : Any> getModel(key: String): T? {
        return FoModels.get(this, key)
    }
}