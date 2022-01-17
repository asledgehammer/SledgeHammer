package com.asledgehammer.framework.cfg

/**
 * **CyclicException** Is thrown when a [CFGSection] detects a cyclic dependency in its hierarchy.
 *
 * @author Jab
 *
 * @param msg The message to display when thrown.
 */
class CyclicException(msg: String) : RuntimeException(msg)
