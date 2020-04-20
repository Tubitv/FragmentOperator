package com.tubitv.fragmentoperator.dialog

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.tubitv.fragmentoperator.fragment.FoFragment
import com.tubitv.fragmentoperator.logging.FoLog
import com.tubitv.fragments.FragmentOperator

open class FoDialog : DialogFragment() {

    companion object {
        private val TAG = FoDialog::class.java.simpleName
        private val mPendingDismiss = HashSet<String>()

        private const val TARGET_FRAGMENT_TAG = "com.tubitv.fragmentoperator.extra.target_fragment_tag"
        private const val REQUEST_CODE = "com.tubitv.fragmentoperator.extra.request_code"
        private const val DIALOG_TAG = "com.tubitv.fragmentoperator.extra.dialog_tag"
        private const val DIALOG_TAG_SEPERATOR = ":"
    }

    /** Standard FoDialog result: operation canceled.  */
    private val RESULT_CANCELED = 0

    private var mDialogTag: String? = null // Tag for current DialogFragment instance
    private val mResultData: HashMap<String, Any> = hashMapOf()
    private var mRequestCode: Int? = null
    private var mResultCode: Int? = null
    private var mTargetFragmentTag: String? = null
    private var mOperationReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mOperationReady = false
        super.onSaveInstanceState(outState)

        // Save data, so we don't lose them during fragment recreation
        outState.putString(DIALOG_TAG, getDialogTag())
        outState.putString(TARGET_FRAGMENT_TAG, mTargetFragmentTag)
        mRequestCode?.let { outState.putInt(REQUEST_CODE, it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mOperationReady = true

        restoreInstanceState(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mOperationReady = true
        dismissIfPending()
    }

    override fun onResume() {
        super.onResume()
        mOperationReady = true
        dismissIfPending()
    }

    override fun onPause() {
        mOperationReady = false
        super.onPause()
    }

    override fun onStop() {
        mOperationReady = false
        super.onStop()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val requestCode = mRequestCode ?: return
        val resultCode = mResultCode ?: RESULT_CANCELED
        val targetFragmentTag = mTargetFragmentTag ?: return

        val targetFragment = FragmentOperator.findActiveFragmentByTag(targetFragmentTag) ?: return

        targetFragment.onDialogFragmentResult(requestCode, resultCode, mResultData)
    }

    override fun dismiss() {
        if (mOperationReady) {
            super.dismiss()
        } else {
            FoLog.i(TAG, "Not ready for dismissing the FoDialog ${getDialogTag()} ")
            mPendingDismiss.add(getDialogTag())
        }
    }

    /**
     * Return the unique tag each DialogFragment instance
     * Since DialogFragment can be re-created when activity configuration changed (i.e. screen rotation) and hashCode will
     * change as well, we should only and always use the initial hashCode.
     *
     * @return Unique tag string per instance
     */
    fun getDialogTag(): String {
        var dialogTag = mDialogTag
        if (dialogTag == null) {
            dialogTag = TAG + DIALOG_TAG_SEPERATOR + this.hashCode()
        }
        mDialogTag = dialogTag
        return dialogTag
    }

    /**
     * Set result for {@link FoFragment#onDialogFragmentResult} and dismiss
     */
    fun setResultAndDismiss(resultCode: Int) {
        mResultCode = resultCode
        dismiss()
    }

    /**
     * Set result for {@link FoFragment#onDialogFragmentResult} and dismiss
     */
    fun setResultAndDismiss(resultCode: Int, arguments: Map<String, Any>) {
        mResultCode = resultCode
        mResultData.putAll(arguments)
        dismiss()
    }

    /**
     * Set the target and request code.
     */
    fun setTargetAndCode(targetFragment: FoFragment, requestCode: Int) {
        mTargetFragmentTag = targetFragment.getFragmentTag()
        mRequestCode = requestCode
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        mDialogTag = savedInstanceState.getString(DIALOG_TAG)
        mTargetFragmentTag = savedInstanceState.getString(TARGET_FRAGMENT_TAG)
        if (savedInstanceState.containsKey(REQUEST_CODE)) {
            mRequestCode = savedInstanceState.getInt(REQUEST_CODE)
        }
    }

    private fun dismissIfPending() {
        if (mPendingDismiss.contains(getDialogTag())) {
            FoLog.i(TAG, "FoDialog ${getDialogTag()} executed the pending dismiss in the onStart")
            mPendingDismiss.remove(getDialogTag())
            dismiss()
        }
    }
}