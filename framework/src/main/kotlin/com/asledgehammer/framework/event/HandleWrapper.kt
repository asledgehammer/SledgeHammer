@file:Suppress("LeakingThis")

package com.asledgehammer.framework.event

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @param A The type of annotation that stores properties of how to handle.
 * @param E The type of parameter to pass to the method.
 *
 * @property listener
 * @property annotation
 * @param method
 */
abstract class HandleWrapper<A : Annotation, E>(val listener: Listener, val annotation: A, method: Method) {

    /**
     * TODO: Document.
     */
    val timeCreated = System.currentTimeMillis()

    /**
     * TODO: Document.
     */
    var isEnabled = false

    private var methodArgumentsCache: Array<Any?> = emptyArray()
    private var methodHandle: MethodHandle? = null
    private var methodParameters: Array<Class<*>> = emptyArray()
    private var methodType: MethodType? = null
    private var methodName: String? = null
    private var isStatic = false

    init {
        method.isAccessible = true
        methodName = method.name
        methodParameters = method.parameterTypes

        require(methodParameters.size == 1) {
            "There is more than one parameter in the method."
        }

        require(isParameterValid(method.parameters[0].type)) {
            "The parameter type is invalid for the handle."
        }

        isStatic = Modifier.isStatic(method.modifiers)
        methodType = MethodType.methodType(method.returnType, methodParameters)

        try {
            methodHandle = LOOKUP.unreflect(method)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        // If the event handler method is static, the parameter cache does not require an instance
        // of the declaring class or anonymous class to invoke.
        if (isStatic) {
            methodArgumentsCache = arrayOfNulls(1)
        } else {
            methodArgumentsCache = arrayOfNulls(2)
            methodArgumentsCache[0] = listener
        }

        isEnabled = true
    }

    /**
     * Dispatches an Event.
     *
     * @param element The Event passed to handle.
     * @throws Throwable Thrown if the event handler fails to handle the Event, or the MethodHandle
     * fails to invoke.
     */
    @Throws(Throwable::class)
    open fun dispatch(element: E) {
        if (!isEnabled) return
        if (!canDispatch(element)) return
        if (isStatic) {
            methodArgumentsCache[0] = element
            methodHandle!!.invokeWithArguments(*methodArgumentsCache)
            methodArgumentsCache[0] = null
        } else {
            methodArgumentsCache[1] = element
            methodHandle!!.invokeWithArguments(*methodArgumentsCache)
            methodArgumentsCache[1] = null
        }
    }

    abstract fun canDispatch(element: E): Boolean

    abstract fun isParameterValid(clazz: Class<*>): Boolean

    companion object {
        protected val LOOKUP: MethodHandles.Lookup = MethodHandles.lookup()
    }
}
