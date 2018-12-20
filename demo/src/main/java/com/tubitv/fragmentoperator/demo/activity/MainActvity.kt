package com.tubitv.fragmentoperator.demo.activity

import android.os.Bundle
import com.tubitv.fragmentoperator.R
import com.tubitv.fragmentoperator.activity.FoActivity
import com.tubitv.fragmentoperator.demo.fragment.MainFragment
import com.tubitv.fragments.FragmentOperator


class MainActvity : FoActivity() {

    companion object {
        private val TAG = MainActvity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FragmentOperator.showFragment(MainFragment())
    }


    override fun getFragmentContainerResId(): Int {
        return R.id.fragment_container
    }
}