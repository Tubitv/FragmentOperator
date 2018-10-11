package com.tubitv.fragmentoperator.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment

open class FoDialog : DialogFragment() {
    private val TAG = FoDialog::class.simpleName

    private val DIALOG_TAG = "dialog_tag"
    private val DIALOG_TAG_SEPERATOR = ":"

    private var mDialogTag: String? = null // Tag for current DialogFragment instance

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save previous fragment tag and current fragment tag, so we don't lose them during fragment recreation
        outState.putString(DIALOG_TAG, getDialogTag())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            mDialogTag = savedInstanceState.getString(DIALOG_TAG)
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
}