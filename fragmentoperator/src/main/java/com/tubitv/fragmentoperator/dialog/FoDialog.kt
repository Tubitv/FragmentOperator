package com.tubitv.fragmentoperator.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragments.FragmentOperator

open class FoDialog : DialogFragment() {
    private val TAG = FoDialog::class.simpleName

    private val TARGET_FRAGMENT_TAG = "target_fragment_tag"
    private val REQUEST_CODE = "request_code"
    private val DIALOG_TAG = "dialog_tag"
    private val DIALOG_TAG_SEPERATOR = ":"

    private var mDialogTag: String? = null // Tag for current DialogFragment instance
    private val mArguments: HashMap<String, Any> = hashMapOf()
    private var mRequestCode: Int? = null
    private var mResultCode: Int? = null
    private var mTargetFragmentTag: String? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save data, so we don't lose them during fragment recreation
        outState.putString(DIALOG_TAG, getDialogTag())
        outState.putString(TARGET_FRAGMENT_TAG, mTargetFragmentTag)
        mRequestCode?.let { outState.putInt(REQUEST_CODE, it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            mDialogTag = savedInstanceState.getString(DIALOG_TAG)
            mTargetFragmentTag = savedInstanceState.getString(TARGET_FRAGMENT_TAG)
            if (savedInstanceState.containsKey(REQUEST_CODE)) {
                mRequestCode = savedInstanceState.getInt(REQUEST_CODE)
            }
        }
    }

    /**
     * Return the unique tag each DialogFragment instance
     * Since DialogFragment can be re-created when activity configuration changed (i.e. screen rotation) and hashCode will
     * change as well, we should only and always use the initial hashCode.
     *
     * @return Unique tag string per instance
     */
    fun getDialogTag(): String? {
        if (mDialogTag == null) {
            mDialogTag = this.javaClass.simpleName + DIALOG_TAG_SEPERATOR + this.hashCode()
        }
        return mDialogTag
    }

    /**
     * Set result for {@link FoFragment#onDialogFragmentResult}
     */
    fun setResult(resultCode: Int) {
        mResultCode = resultCode
    }

    /**
     * Set result for {@link FoFragment#onDialogFragmentResult}
     */
    fun setResult(resultCode: Int, arguments: HashMap<String, Any>) {
        mResultCode = resultCode
        mArguments.putAll(arguments)
    }

    /**
     * Get map to pass values to a Fragment.
     */
    fun getDataMap(): HashMap<String, Any> {
        return mArguments
    }

    /**
     * Set the target and request code.
     */
    fun setTargetAndCode(targetFragment: FoFragment, requestCode: Int) {
        mTargetFragmentTag = targetFragment.getFragmentTag()
        mRequestCode = requestCode
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        if (mRequestCode == null || mResultCode == null || mTargetFragmentTag == null) return

        val targetFragment = FragmentOperator.findFoFragmentByTag(mTargetFragmentTag!!) ?: return

        targetFragment.onDialogFragmentResult(mRequestCode!!, mResultCode!!, mArguments)
    }
}