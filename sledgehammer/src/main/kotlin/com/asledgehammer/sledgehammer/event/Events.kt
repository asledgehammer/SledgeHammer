@file:Suppress("unused", "UNCHECKED_CAST")

package com.asledgehammer.sledgehammer.event

import com.asledgehammer.sledgehammer.Sledgehammer
import com.asledgehammer.sledgehammer.command.*
import java.lang.reflect.Method
import java.util.*

/**
 * **Events** TODO: Document.
 *
 * @author Jab
 */
object Events {

  private val wrappers = HashMap<UUID, Wrapper>()
  private val sortedCommandWrappers = HashMap<String, ArrayList<CommandHandleWrapper>>()
  private val sortedEventWrappers = HashMap<Class<out Event>, ArrayList<EventHandleWrapper>>()
  private val eventWrappersToRemove = ArrayList<EventHandleWrapper>()
  private val commandWrappersToRemove = ArrayList<CommandHandleWrapper>()

  /** TODO: Document. */
  @JvmStatic
  fun register(id: UUID, listener: Listener) {
    wrappers.computeIfAbsent(id) { Wrapper() }.register(listener)
    sort()
  }

  /** TODO: Document. */
  @JvmStatic
  fun unregister(id: UUID) {
    val wrapper = wrappers.remove(id) ?: return
    wrapper.reset()
  }

  /** TODO: Document. */
  @JvmOverloads
  @JvmStatic
  fun unregister(listener: Listener, sort: Boolean = true) {
    for ((_, wrapper) in wrappers) wrapper.unregister(listener)
    if (sort) sort()
  }

  /** TODO: Document. */
  @JvmStatic
  fun dispatch(event: Event) {
    val clazz = event.javaClass
    val wrappers = sortedEventWrappers[clazz] ?: return
    for (wrapper in wrappers) {
      try {
        wrapper.dispatch(event)
      } catch (throwable: Throwable) {
        Sledgehammer.logError("Failed to execute listener: ${clazz.simpleName}. Disabling listener.", throwable)
        throwable.printStackTrace(System.err)
        wrapper.isEnabled = false
        eventWrappersToRemove.add(wrapper)
      }
    }
    // Remove any disabled wrappers.
    if (eventWrappersToRemove.isNotEmpty()) {
      for (wrapper in eventWrappersToRemove) unregister(wrapper.listener, false)
      sort()
    }
  }

  /** TODO: Document. */
  @JvmStatic
  fun dispatch(command: Command, commander: Commander): CommandExecution {
    val response = Command.Response()
    val key = command.name.toLowerCase().trim()
    val execution = CommandExecution(command, commander, response)
    val wrappers = sortedCommandWrappers[key] ?: return execution
    response.found = true
    for (wrapper in wrappers) {
      try {
        wrapper.dispatch(execution)
        if (response.denied) break
      } catch (throwable: Throwable) {
        Sledgehammer.logError("Failed to execute listener: ${wrapper.javaClass.simpleName}. Disabling.")
        throwable.printStackTrace(System.err)
        wrapper.isEnabled = false
        commandWrappersToRemove.add(wrapper)
      }
    }
    // Remove any disabled wrappers.
    if (commandWrappersToRemove.isNotEmpty()) {
      for (wrapper in commandWrappersToRemove) unregister(wrapper.listener, false)
      sort()
    }
    return execution
  }

  /** TODO: Document. */
  @JvmStatic
  fun reset() {
    for ((_, wrapper) in wrappers) wrapper.reset()
    wrappers.clear()
  }

  private fun sort() {
    sortedCommandWrappers.clear()
    sortedEventWrappers.clear()
    for ((_, wrapper) in wrappers) {
      for ((key, collection) in wrapper.commandWrappers) {
        val next = sortedCommandWrappers.computeIfAbsent(key) { ArrayList() }
        for (handle in collection) next.add(handle)
      }
      for ((key, collection) in wrapper.eventWrappers) {
        val next = sortedEventWrappers.computeIfAbsent(key) { ArrayList() }
        for (handle in collection) next.add(handle)
      }
    }
    for ((_, list) in sortedCommandWrappers) list.sortByDescending { it.annotation.priority }
    for ((_, list) in sortedEventWrappers) list.sortByDescending { it.annotation.priority }
  }

  /**
   * **Wrapper** TODO: Document.
   *
   * @author Jab
   */
  private class Wrapper {

    val eventWrappers = HashMap<Class<out Event?>, ArrayList<EventHandleWrapper>>()
    val commandWrappers = HashMap<String, ArrayList<CommandHandleWrapper>>()

    /** TODO: Document. */
    fun register(listener: Listener) {
      // Grab the methods for the Listener.
      val methods: List<Method> = getAllDeclaredMethods(listener.javaClass)
      if (methods.isEmpty()) return
      for (method in methods) {
        val eventHandler = method.getAnnotation(EventHandler::class.java)
        if (eventHandler != null) {
          val clazz = method.parameters[0].type as Class<out Event?>
          val wrapper = EventHandleWrapper(listener, eventHandler, method)
          eventWrappers.computeIfAbsent(clazz) { ArrayList() }.add(wrapper)
          continue
        }
        val commandHandler = method.getAnnotation(CommandHandler::class.java)
        if (commandHandler != null) {
          val wrapper = CommandHandleWrapper(listener, commandHandler, method)
          for (command in commandHandler.commands) {
            val key = command.toLowerCase().trim()
            commandWrappers.computeIfAbsent(key) { ArrayList() }.add(wrapper)
          }
        }
      }
    }

    /** TODO: Document. */
    fun unregister(listener: Listener) {
      val commandsToRemove = ArrayList<String>()
      for ((command, wrapper) in commandWrappers) {
        val iterator = wrapper.iterator()
        while (iterator.hasNext()) {
          val next = iterator.next()
          if (next.listener == listener) iterator.remove()
        }
        if (wrapper.isEmpty()) commandsToRemove.add(command)
      }
      if (commandsToRemove.isNotEmpty()) {
        for (command in commandsToRemove) commandWrappers.remove(command)
      }

      val eventsToRemove = ArrayList<Class<out Event>>()
      for ((clazz, wrapper) in eventWrappers) {
        val iterator = wrapper.iterator()
        while (iterator.hasNext()) {
          val next = iterator.next()
          if (next.listener == listener) iterator.remove()
        }
        if (wrapper.isEmpty()) eventsToRemove.add(clazz)
      }
      if (eventsToRemove.isNotEmpty()) {
        for (clazz in eventsToRemove) eventWrappers.remove(clazz)
      }
    }


    /** TODO: Document. */
    fun reset() {
      clearEventListeners()
      clearCommandListeners()
    }

    private fun clearCommandListeners() {
      for ((_, list) in commandWrappers) {
        val iterator: MutableIterator<CommandHandleWrapper> = list.iterator()
        while (iterator.hasNext()) {
          val wrapper: CommandHandleWrapper = iterator.next()
          wrapper.isEnabled = false
          iterator.remove()
        }
      }
      commandWrappers.clear()
    }

    private fun clearEventListeners() {
      for ((_, list) in eventWrappers) {
        val iterator: MutableIterator<EventHandleWrapper> = list.iterator()
        while (iterator.hasNext()) {
          val wrapper: EventHandleWrapper = iterator.next()
          wrapper.isEnabled = false
          iterator.remove()
        }
      }
      eventWrappers.clear()
    }

    private fun getAllDeclaredMethods(clazz: Class<*>): List<Method> {
      val methods = ArrayList<Method>()
      var c: Class<*>? = clazz
      while (c != null) {
        methods.addAll(listOf(*c.declaredMethods))
        c = c.superclass
      }
      return methods
    }
  }
}
