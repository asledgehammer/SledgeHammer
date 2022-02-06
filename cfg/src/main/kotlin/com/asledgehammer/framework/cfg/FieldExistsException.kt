package com.asledgehammer.framework.cfg

/**
 * **FieldAlreadyExistsException** is thrown when a [CFGSection] attempts to create a child section and a field
 * already exists with the name.
 *
 * @author Jab
 *
 * @param msg The message to display when thrown.
 */
class FieldExistsException(msg: String) : RuntimeException(msg)
