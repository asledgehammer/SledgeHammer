package com.asledgehammer.framework.event

/**
 * TODO: Document.
 *
 * @author Jab
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class EventHandler(

    /**
     * TODO: Document.
     */
    val priority: Int = 0,

    /**
     * TODO: Document.
     */
    val ignoreCancelled: Boolean = false,
)
