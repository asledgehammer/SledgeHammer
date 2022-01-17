@file:Suppress("unused")

package com.asledgehammer.framework.event.command

/**
 * TODO: Document.
 *
 * @author Jab
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class CommandHandler(

    /**
     * TODO: Document.
     */
    val commands: Array<String>,

    /**
     * TODO: Document.
     */
    val permission: Array<String> = ["*"],

    /**
     * TODO: Document.
     */
    val priority: Int = 0,

    /**
     * TODO: Document.
     */
    val ignoreHandled: Boolean = false,
)
