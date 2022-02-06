package com.asledgehammer.sledgehammer.command

import com.asledgehammer.sledgehammer.event.HandleWrapper
import com.asledgehammer.sledgehammer.event.Listener
import java.lang.reflect.Method

/**
 * **CommandHandleWrapper** TODO: Document.
 *
 * @author Jab
 *
 * @param listener The declaring instance.
 * @param annotation The information for handling the event.
 * @param method The method handler to invoke.
 */
class CommandHandleWrapper(listener: Listener, annotation: CommandHandler, method: Method) :
  HandleWrapper<CommandHandler, CommandExecution>(listener, annotation, method) {

  override fun canDispatch(element: CommandExecution): Boolean {
    if (annotation.ignoreHandled) return true
    return !element.response.handled
  }

  override fun isParameterValid(clazz: Class<*>): Boolean = CommandExecution::class.java.isAssignableFrom(clazz)
}
