package com.tubitv.fragmentoperator.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tubitv.fragments.FragmentOperator

abstract class FoActivity : AppCompatActivity() {
    private val TAG = FoActivity::class.simpleName

    private var mIsForeground = false
    private var mFragmentManagerPrepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsForeground = true
        mFragmentManagerPrepared = true
        FragmentOperator.setCurrentActivity(this)
    }

    override fun onResume() {
        super.onResume()
        mIsForeground = true
        mFragmentManagerPrepared = true
        FragmentOperator.setCurrentActivity(this)
    }

    override fun onPause() {
        super.onPause()
        mIsForeground = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mFragmentManagerPrepared = false
    }

    override fun onBackPressed() {
        if (!FragmentOperator.onBackPressed()) {
            super.onBackPressed()
        }
    }

    fun isForeground(): Boolean {
        return mIsForeground
    }

    fun isReadyForFragmentOperation(): Boolean {
        return mIsForeground && mFragmentManagerPrepared
    }

    abstract fun getFragmentContainerResId(): Int
}