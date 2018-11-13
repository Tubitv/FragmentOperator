package com.tubitv.fragments

import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.tubitv.fragmentoperator.activity.FoActivity
import com.tubitv.fragmentoperator.dialog.FoDialog
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.fragment.SingleInstanceFragment
import com.tubitv.fragmentoperator.logging.FoLog
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

    /**
     * Set current active activity
     */
    fun setCurrentActivity(activity: FoActivity) {
        mActivityRef = WeakReference(activity)
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

        // If activity is null, just return
        val activity = getCurrentActivity() ?: return

        // If activity is not active, we shouldn't show any fragment
        if (!activity.isReadyForFragmentOperation()) {
            // TODO: cache UI actions and resume later
            return
        }

        if (containerId == 0) {
            FoLog.d(TAG, "showFragment fail due to empty container id")
        }

        if (clearStack) {
            activity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()

        // Fragment transition animation
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        // Handle single instance
        val previousFragmentTagForOverride = handleSingleInstanceFragment(fragment)

        val fragmentTag = fragment.getFragmentTag()

        // Once fragment tag is set to transaction, it doesn't change and always refers to the specific fragment.
        // Fragment tag will stay the same even when fragment got recreated
        fragmentTransaction.replace(containerId, fragment, fragmentTag) // Set tag for retrieve in findFragmentByTag
        fragmentTransaction.addToBackStack(fragmentTag) // Reuse the same tag for fragment name

        fragment.skipOnPop = skipOnPop

        // If back stack order has changed, we need check if previous fragment tag need to be overridden
        if (previousFragmentTagForOverride == null) {

            val currentFragment = getCurrentFragment(containerId)

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

    /**
     * Handle back pressed for fragment routing
     *
     * @return True if fragment pop back is performed
     */
    fun onBackPressed(): Boolean {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "handle onBackPressed fail due to current activity is null")
            return false
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "handle onBackPressed fail due to current activity is not ready for fragment operation")
            return false
        }

        val currentFragment = getCurrentFragment() ?: kotlin.run {
            FoLog.d(TAG, "handle onBackPressed fail due to current fragment is null")
            return false
        }

        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {

        }

        // When currently it's the last fragment, we should just go back to phone home screen
        if (activity.supportFragmentManager.backStackEntryCount == 1) {
            val pauseAppIntent = Intent(Intent.ACTION_MAIN)
            pauseAppIntent.addCategory(Intent.CATEGORY_HOME)
            pauseAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(pauseAppIntent)
            return true
        }

        val previousFragmentTag = currentFragment.previousFragmentTag
        if (previousFragmentTag != null) {
            return popToFragment(previousFragmentTag)
        }

        return false
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
    private fun handleSingleInstanceFragment(fragment: FoFragment): String? {
        if (hasAnnotation(fragment::class.java, SingleInstanceFragment::class.java)) {
            val frag = findFragmentInBackStack(fragment::class.java)
            val previousFragmentTag = frag?.previousFragmentTag
            if (previousFragmentTag != null) {
                // If we already have an instance in backstack, pop to the previous one and load the new instance later
                popToFragment(previousFragmentTag)
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
     * Default container id to {@link R.id.activity_abstract_drawer_container}, since we
     * are going to use single activity
     */
    private fun getCurrentFragment(): FoFragment? {
        val activity = getCurrentActivity() ?: return null
        return getCurrentFragment(activity.getFragmentContainerResId())
    }

    /**
     * Get current fragment on the top of back stack
     */
    private fun getCurrentFragment(@IdRes containerId: Int): FoFragment? {
        val activity = getCurrentActivity() ?: return null

        if (!activity.isReadyForFragmentOperation()) {
            return null
        }

        val fragment = activity.supportFragmentManager.findFragmentById(containerId)

        if (fragment != null && fragment is FoFragment) {

            return fragment
        }

        return null
    }

    /**
     * Pop to a fragment with specific tag
     */
    private fun popToFragment(fragmentTag: String): Boolean {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "popToFragment fail due to current activity is null")
            return false
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "popToFragment fail due to current activity is not ready for fragment operation")
            return false
        }

        if (activity.supportFragmentManager.findFragmentByTag(fragmentTag) != null) {
            FoLog.d(TAG, "popToFragment found tag: $fragmentTag")
            activity.supportFragmentManager.popBackStack(fragmentTag, 0)
            return true
        } else {
            FoLog.d(TAG, "popToFragment tag not found: $fragmentTag")
        }

        return false

    }

    /**
     * Find fragment instance with give fragment class
     */
    private fun findFragmentInBackStack(fragmentClass: Class<*>): FoFragment? {
        val activity = getCurrentActivity() ?: kotlin.run {
            FoLog.d(TAG, "findFragmentInBackStack fail due to current activity is null")
            return null
        }

        if (!activity.isReadyForFragmentOperation()) {
            FoLog.d(TAG, "findFragmentInBackStack fail due to current activity is not ready for fragment operation")
            return null
        }

        val fragmentManager = activity.supportFragmentManager

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