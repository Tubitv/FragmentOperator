package com.tubitv.fragmentoperator.fragment.annotation


/**
 * Annotation for fragment tab navigation
 *
 * @param tabIndex tab index for which tab to show the fragment instance, invalid index including -1 will be displayed
 *                  at current tab
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TabChildFragment(val tabIndex: Int)