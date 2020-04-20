package com.tubitv.fragmentoperator.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.tubitv.fragmentoperator.interfaces.FragmentHost
import com.tubitv.fragments.FragmentOperator

abstract class FoActivity : AppCompatActivity(), FragmentHost {
    companion object {
        private val TAG = FoActivity::class.simpleName
        private const val FRAGMENT_MANAGER_DEFAULT_TAG = "activity_fragment_manager"
    }

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
        // Only let activity handles backPress if FragmentOperator doesn't handle and FragmentManager is ready
        if (!FragmentOperator.onBackPressed() && mFragmentManagerPrepared) {
            super.onBackPressed()
        }
    }

    override fun isReadyForFragmentOperation(): Boolean {
        return mIsForeground && mFragmentManagerPrepared
    }

    override fun getFragmentManagerTag(): String {
        return this::class.simpleName ?: FRAGMENT_MANAGER_DEFAULT_TAG
    }

    override fun getHostFragmentManager(): FragmentManager {
        return supportFragmentManager
    }

    fun isForeground(): Boolean {
        return mIsForeground
    }

    abstract fun getFragmentContainerResId(): Int
}