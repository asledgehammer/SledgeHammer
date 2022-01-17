package com.asledgehammer.framework.cfg

/**
 * **UnresolvedException** is thrown when a query in a [CFGSection] fails to resolve.
 *
 * @author Jab
 *
 * @param msg The message to display when thrown.
 */
class UnresolvedException(msg: String) : RuntimeException(msg)
