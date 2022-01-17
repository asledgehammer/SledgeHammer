package com.asledgehammer.framework.event

import java.lang.reflect.Method

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @param listener The declaring instance.
 * @param annotation The information for handling the event.
 * @param method The method handler to invoke.
 */
class EventHandleWrapper(listener: Listener, annotation: EventHandler, method: Method) :
    HandleWrapper<EventHandler, Event>(listener, annotation, method) {

    override fun canDispatch(element: Event): Boolean {
        if (annotation.ignoreCancelled) return true
        return element !is CancelableEvent || !element.cancelled
    }

    override fun isParameterValid(clazz: Class<*>): Boolean = Event::class.java.isAssignableFrom(clazz)
}
