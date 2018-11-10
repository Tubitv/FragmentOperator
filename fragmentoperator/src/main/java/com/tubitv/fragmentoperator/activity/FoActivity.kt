package com.tubitv.fragmentoperator.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class FoActivity : AppCompatActivity() {
    private val TAG = FoActivity::class.simpleName

    private var isForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isForeground = true
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    fun isForeground(): Boolean {
        return isForeground
    }

    abstract fun getFragmentContainerResId(): Int
}