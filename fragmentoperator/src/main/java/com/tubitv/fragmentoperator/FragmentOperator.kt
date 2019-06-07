package com.tubitv.fragments

import android.content.Intent
import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.tubitv.fragmentoperator.activity.FoActivity
import com.tubitv.fragmentoperator.dialog.FoDialog
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.fragment.annotation.SingleInstanceFragment
import com.tubitv.fragmentoperator.fragment.annotation.TabChildFragment
import com.tubitv.fragmentoperator.interfaces.FragmentHost
import com.tubitv.fragmentoperator.interfaces.TabsNavigator
import com.tubitv.fragmentoperator.logging.FoLog
import com.tubitv.fragmentoperator.models.FoModels
import java.lang.ref.WeakReference

/**
 * Handle all fragment transaction related logic
 *
 * We currently have three types of fragments:
 *  1. Normal fragment: load and pop in regular order and follow standard Android back stack
 *  2. Skip on back fragment: use a per instance flag and will be skipped when pop back stack. Example: A->B->C, B is
 *      marked as skipOnPop, when press back button on C, it will skip B and go to A directly
 *  3. Single instance fragment: use a per class annotation {@link SingleInstanceFragment}. When load an instance, it
 *      will check if another instance exists in back stack. If another instance does exist, it will pop to the previous
 *      fragment of the existing instance then load the new instance. This will guarantee only at most one instance
 *      exists in back stack
 */
object FragmentOperator {
    private val TAG = FragmentOperator::class.simpleName

    private var mActivityRef: WeakReference<FoActivity>? = null
    private var mTabsNavigator: TabsNavigator? = null
    private val mPendingChildFragmentList: MutableList<FoFragment> = ArrayList()

    /**
     * Set current active activity
     */
    fun setCurrentActivity(activity: FoActivity) {
        mActivityRef = WeakReference(activity)
    }

    fun registerTabsNavigator(tabsNavigator: TabsNavigator) {
        mTabsNavigator = tabsNavigator
    }

    fun unregisterTabsNavigator() {
        mTabsNavigator = null
    }

    fun getTabsNavigator(): TabsNavigator? {
        return mTabsNavigator
    }

    /**
     * Show a fragment normally
     *
     * @param fragment Fragment instance to be displayed
     */
    fun showFragment(fragment: FoFragment) {
        showFragment(fragment, false)
    }

    /**
     * Show a fragment and control if clear all previous back stack
     *
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     */
    fun showFragment(fragment: FoFragment,
                     clearStack: Boolean) {
        showFragment(fragment, clearStack, false)
    }

    /**
     * Show a fragment with control of clearing previous back stack and setting fragment to be skipped on back
     *
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     * @param skipOnPop Flag for fragment to be skipped when pop back stack
     */
    fun showFragment(fragment: FoFragment,
                     clearStack: Boolean,
                     skipOnPop: Boolean) {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "showFragment fail due to current activity is null")
            return
        }

        // check if this fragment belongs to a tab
        if (hasAnnotation(fragment::class.java, TabChildFragment::class.java)) {

            val tabsNavigator = mTabsNavigator

            if (tabsNavigator == null) {
                FoLog.d(TAG, "showFragment for tabChildFragment fail due to TabsNavigator is null")
                return
            }

            val targetTabIndex = fragment::class.java.getAnnotation(TabChildFragment::class.java).tabIndex

            // If tab index is not valid or matches current tab index, load fragment to current tab
            if (!tabsNavigator.isTabIndexValid(targetTabIndex) || targetTabIndex == tabsNavigator.getCurrentTabIndex()) {
                tabsNavigator.getCurrentContainerFragment()?.showChildFragment(fragment)
            } else { // We need switch tab first then load child fragment
                mPendingChildFragmentList.add(fragment)
                tabsNavigator.switchToTab(targetTabIndex)
            }
            return
        }

        showFragment(fragment, clearStack, skipOnPop, activity.getFragmentContainerResId())
    }

    /**
     * Show a fragment in a container with control of clearing previous back stack and setting fragment to be skipped on back
     *
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     * @param skipOnPop Flag for fragment to be skipped when pop back stack
     * @param containerId Container resource ID to display the fragment
     */
    fun showFragment(fragment: FoFragment,
                     clearStack: Boolean,
                     skipOnPop: Boolean,
                     @IdRes containerId: Int) {

        if (containerId == 0) {
            FoLog.d(TAG, "showFragment fail due to empty container id")
            return
        }
        val activity = getCurrentActivity() ?: return
        showFragmentWithFragmentHost(activity, fragment, clearStack, skipOnPop, containerId)
    }

    /**
     * Show a fragment in a container fragment
     *
     * @param containerFragment Container Fragment to be used for displaying fragment
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     * @param skipOnPop Flag for fragment to be skipped when pop back stack
     * @param containerId Container resource ID to display the fragment
     */
    fun showFragment(containerFragment: FoFragment,
                     fragment: FoFragment,
                     clearStack: Boolean,
                     skipOnPop: Boolean,
                     @IdRes containerId: Int) {

        if (!containerFragment.isReadyForFragmentOperation()) {
            // TODO: cache UI actions and resume later
            return
        }
        showFragmentWithFragmentHost(containerFragment, fragment, clearStack, skipOnPop, containerId)
    }

    /**
     * Show a fragment in a container with control of clearing previous back stack and setting fragment to be skipped on back
     *
     * @param fragmentHost FragmentHost to be used for displaying fragment
     * @param fragment Fragment instance to be displayed
     * @param clearStack Flag to clear back stack or not
     * @param skipOnPop Flag for fragment to be skipped when pop back stack
     * @param containerId Container resource ID to display the fragment
     */
    fun showFragmentWithFragmentHost(fragmentHost: FragmentHost,
                                     fragment: FoFragment,
                                     clearStack: Boolean,
                                     skipOnPop: Boolean,
                                     @IdRes containerId: Int) {

        val fragmentManager = fragmentHost.getHostFragmentManager()

        // If activity is null, just return
        val activity = getCurrentActivity() ?: return

        // If activity is not active, we shouldn't show any fragment.
        // We also use activity lifecycle to check if fragment ready for child fragment.
        if (!activity.isReadyForFragmentOperation()) {
            // TODO: cache UI actions and resume later
            FoLog.d(TAG, "Activity FragmentManager is not ready to show fragment")
            return
        }

        if (clearStack) {
            fragmentHost.handlePopBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        val fragmentTransaction = fragmentManager.beginTransaction()

        // Fragment transition animation
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        // Handle single instance
        val previousFragmentTagForOverride = handleSingleInstanceFragment(fragmentHost, fragment)

        val fragmentTag = fragment.getFragmentTag()

        // Once fragment tag is set to transaction, it doesn't change and always refers to the specific fragment.
        // Fragment tag will stay the same even when fragment got recreated
        fragmentTransaction.replace(containerId, fragment, fragmentTag) // Set tag for retrieve in findFragmentByTag
        fragmentTransaction.addToBackStack(fragmentTag) // Reuse the same tag for fragment name

        fragment.skipOnPop = skipOnPop

        // If back stack order has changed, we need check if previous fragment tag need to be overridden
        if (previousFragmentTagForOverride == null) {

            val currentFragment = getCurrentFragment(fragmentManager, containerId)

            if (currentFragment != null) {
                if (currentFragment.skipOnPop) {
                    // If we need skip current fragment on back, point new fragment to current fragment's previous fragment
                    fragment.previousFragmentTag = currentFragment.previousFragmentTag
                } else {
                    // Otherwise, always point to current fragment
                    fragment.previousFragmentTag = currentFragment.getFragmentTag()
                }
            }
        } else {
            // Because pop fragment in handle single instance take a while, getCurrentFragment is not the new fragment after pop
            // So we just grab the new previous fragment tag
            fragment.previousFragmentTag = previousFragmentTagForOverride
        }

        // Save tag to track which FragmentManager the fragment belongs to
        fragment.addHostFragmentManagerTag(fragmentHost.getFragmentManagerTag())

        // Save models so we can recover when fragments get recreated
        FoModels.add(fragment, fragment.getAllArguments())

        fragmentTransaction.commit()
    }

    fun showDialog(dialog: FoDialog) {
        // If activity is null, just return
        val activity = getCurrentActivity() ?: return

        // If activity is not active, we shouldn't show any fragment
        if (!activity.isReadyForFragmentOperation()) {
            // TODO: cache UI actions and resume later
            return
        }

        dialog.show(activity.supportFragmentManager, dialog.getDialogTag())
    }

    fun handlePendingChildFragments(containerFragment: FoFragment) {
        if (!mPendingChildFragmentList.isEmpty()) {
            for (fragment in mPendingChildFragmentList) {
                containerFragment.showChildFragment(fragment)
            }
            mPendingChildFragmentList.clear()
        }
    }

    /**
     * Handle back pressed for fragment routing
     *
     * @return True if fragment pop back is performed
     */
    fun onBackPressed(): Boolean {
        // TODO handle child fragment backstack
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "handle onBackPressed fail due to current activity is null")
            return false
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "handle onBackPressed fail due to current activity is not ready for fragment operation")
            return false
        }

        val currentFragment = getCurrentFragment(activity.supportFragmentManager, activity.getFragmentContainerResId())
                ?: kotlin.run {
                    FoLog.d(TAG, "handle onBackPressed fail due to current fragment is null")
                    return false
                }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && currentFragment.isStateSaved) {
            FoLog.d(TAG, "The current Fragment Manager has saved its states already")
            return false
        }

        val tabsNavigator = mTabsNavigator
        val currentContainerFragment = mTabsNavigator?.getCurrentContainerFragment()
        if (tabsNavigator != null && currentContainerFragment != null) {
            val currentChildFragment = currentContainerFragment.getCurrentChildFragment()

            // Check if current child fragment wants to handle back pressed
            if (currentChildFragment != null && currentChildFragment.onBackPressed()) {
                return true
            }

            // Check if current tab has more than one fragments
            if (currentContainerFragment.childFragmentManager.backStackEntryCount > 1) {
                if (currentChildFragment != null && currentContainerFragment.isReadyForFragmentOperation()) {
                    return popToPreviousFragment(currentChildFragment, currentContainerFragment)
                }
            } else if (tabsNavigator.getCurrentTabIndex() != 0) { // Check if tabs have navigation history TODO handle multiple tab history
                tabsNavigator.switchToTab(0) // Switch to first tab
                return true
            }
        }

        // Check if current fragment wants to handle back pressed
        if (currentFragment.onBackPressed()) {
            return true
        }

        // When currently it's the last fragment and it doesn't handle back pressed, we should just go back to phone home screen
        if (activity.supportFragmentManager.backStackEntryCount == 1) {
            val pauseAppIntent = Intent(Intent.ACTION_MAIN)
            pauseAppIntent.addCategory(Intent.CATEGORY_HOME)
            pauseAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(pauseAppIntent)
            return true
        }

        // Pop main backstack
        return popToPreviousFragment(currentFragment, activity)
    }

    /**
     * @return True if successfully navigate back to root fragment, false if already on root fragment
     */
    fun handleTabPopToRootFragment(): Boolean {
        val tabsNavigator = mTabsNavigator
        val currentContainerFragment = mTabsNavigator?.getCurrentContainerFragment()
        if (tabsNavigator != null && currentContainerFragment != null && currentContainerFragment.isReadyForFragmentOperation()) {
            // Check if current tab has more than one fragments
            if (currentContainerFragment.childFragmentManager.backStackEntryCount > 1) {
                currentContainerFragment.getRootChildFragmentTag()?.let { childFragmentTag ->
                    return popToFragment(currentContainerFragment, childFragmentTag)
                }
            }
        }
        return false
    }

    private fun popToPreviousFragment(fragment: FoFragment, fragmentHost: FragmentHost): Boolean {
        val previousFragmentTag = fragment.previousFragmentTag
        if (previousFragmentTag != null) {
            return popToFragment(fragmentHost, previousFragmentTag)
        }
        return false
    }

    /**
     * Get current fragment on the top of back stack
     *
     * @param fragmentManager Fragment manager instance to be used for look up
     * @param containerId Container view id
     */
    fun getCurrentFragment(fragmentManager: FragmentManager?, @IdRes containerId: Int): FoFragment? {
        val fragmentManager = fragmentManager ?: return null

        val activity = getCurrentActivity() ?: return null

        if (!activity.isReadyForFragmentOperation()) {
            return null
        }

        val fragment = fragmentManager.findFragmentById(containerId)

        if (fragment != null && fragment is FoFragment) {

            return fragment
        }

        return null
    }

    private fun getCurrentActivity(): FoActivity? {
        return mActivityRef?.get()
    }

    /**
     * This will check if fragment is annotated with {@link SingleInstanceFragment}
     * If there is already an instance in back stack, it will pop back to the previous fragment of existing instance.
     * Otherwise, do nothing
     *
     * @param fragment The fragment instance to
     * @return Previous fragment tag of existing instance if exists in back stack
     */
    private fun handleSingleInstanceFragment(fragmentHost: FragmentHost,
                                             fragment: FoFragment): String? {

        if (hasAnnotation(fragment::class.java, SingleInstanceFragment::class.java)) {
            val frag = findFragmentInBackStack(fragmentHost.getHostFragmentManager(), fragment::class.java)
            val previousFragmentTag = frag?.previousFragmentTag
            if (previousFragmentTag != null) {
                // If we already have an instance in backstack, pop to the previous one and load the new instance later
                popToFragment(fragmentHost, previousFragmentTag)
                return frag.previousFragmentTag
            }
        }

        return null
    }

    /**
     * Check if fragment class has given class level annotation
     */
    private fun hasAnnotation(fragmentClass: Class<*>, annotationClass: Class<*>): Boolean {
        for (annotation in fragmentClass.annotations) {
            if (annotation.annotationClass.java == annotationClass) {
                return true
            }
        }
        return false
    }

    /**
     * Pop to a fragment with specific tag
     */
    private fun popToFragment(fragmentHost: FragmentHost, fragmentTag: String): Boolean {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "popToFragment fail due to current activity is null")
            return false
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "popToFragment fail due to current activity is not ready for fragment operation")
            return false
        }

        val fragmentManager = fragmentHost.getHostFragmentManager()

        if (fragmentManager.findFragmentByTag(fragmentTag) != null) {
            FoLog.d(TAG, "popToFragment found tag: $fragmentTag")

            fragmentHost.handlePopBackStack(fragmentTag, 0)
            return true
        } else {
            FoLog.d(TAG, "popToFragment tag not found: $fragmentTag")
        }

        return false

    }

    /**
     * Find fragment instance with give fragment class
     */
    private fun findFragmentInBackStack(fragmentManager: FragmentManager, fragmentClass: Class<*>): FoFragment? {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "findFragmentInBackStack fail due to current activity is null")
            return null
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "findFragmentInBackStack fail due to current activity is not ready for fragment operation")
            return null
        }

        // Check visible fragments
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (frag in fragmentManager.fragments) {
                if (fragmentClass.isInstance(frag)
                        && FoFragment::class.isInstance(frag)) {
                    return frag as FoFragment
                }
            }
        }

        // Check fragments in back stack
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val frag = fragmentManager
                    .findFragmentByTag(fragmentManager.getBackStackEntryAt(i).name)
            if (fragmentClass.isInstance(frag)
                    && FoFragment::class.isInstance(frag)) {
                return frag as FoFragment
            }
        }

        return null
    }
}