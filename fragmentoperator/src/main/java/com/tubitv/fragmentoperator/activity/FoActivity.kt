package com.tubitv.fragmentoperator.activity

import android.os.Bundle
import android.os.PersistableBundle
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
        FragmentOperator.setCurrentActivity(this)
    }

    override fun onPostResume() {
        super.onPostResume()
        mFragmentManagerPrepared = true
    }

    override fun onPause() {
        super.onPause()
        mIsForeground = false
        mFragmentManagerPrepared = false
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        mFragmentManagerPrepared = false
        super.onSaveInstanceState(outState, outPersistentState)
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