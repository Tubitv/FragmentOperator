package com.tubitv.fragmentoperator.fragment.annotation


/**
 * Annotation for fragment that only allow one instance in backstack
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleInstanceFragment