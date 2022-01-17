@file:Suppress("unused")

package com.asledgehammer.framework.permissions

import java.util.*

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @property id
 * @param name
 */
abstract class PermissionCollection(val id: UUID = UUID.randomUUID(), name: String) {

    var permissions = HashMap<String, Permission>()
    val name: String = name.toLowerCase().trim()

    override fun toString(): String = "PermissionCollection(name=$name, id=$id)"

    /**
     * TODO: Document.
     */
    fun add(permission: Permission) {
        permissions[permission.context]?.flag = permission.flag
        permissions[permission.context] = permission
    }

    /**
     * TODO: Document.
     */
    fun remove(permission: Permission) {
        permissions.remove(permission.context)
    }

    /**
     * TODO: Document.
     */
    fun get(context: String): Permission? = getExplicit(context) ?: getClosest(context)

    /**
     * TODO: Document.
     */
    fun set(context: String, flag: Boolean): Permission {
        var node = getExplicit(context)
        if (node != null) {
            node.flag = flag
        } else {
            node = Permission(context, flag)
            permissions[context] = node
        }
        return node
    }

    /**
     * TODO: Document.
     */
    fun set(permissions: Collection<Permission>) {
        for (permission in permissions) set(permission)
    }

    /**
     * TODO: Document.
     */
    fun set(vararg permissions: Permission) {
        for (permission in permissions) {
            val node = getExplicit(permission.context)
            if (node != null) node.flag = permission.flag
            this.permissions[permission.context] = permission
        }
    }

    /**
     * TODO: Document.
     */
    fun hasAny(context: String): Boolean {
        val subNodes = getAllSub(context)
        if (subNodes.isNotEmpty()) {
            val subNodesGranted = getGrantedNodes(subNodes)
            if (subNodesGranted.isNotEmpty()) return true
        }
        return false
    }

    /**
     * TODO: Document.
     */
    fun getSub(context: String): List<Permission> {
        val formattedContext = context.toLowerCase().trim()
        require(formattedContext.isNotEmpty()) { "The context is empty." }
        val list = ArrayList<Permission>()
        for ((_, next) in permissions) if (next.isSub(formattedContext)) list.add(next)
        return list
    }

    /**
     * TODO: Document.
     */
    fun hasSub(context: String): Boolean {
        val node = context.toLowerCase().trim()
        require(node.isNotEmpty()) { "The context is empty." }
        for ((_, next) in permissions) if (next.isSub(node)) return true
        return false
    }

    /**
     * TODO: Document.
     */
    open fun getAllSub(context: String): List<Permission> {
        val formattedContext = context.toLowerCase().trim()
        require(formattedContext.isNotEmpty()) { "The context is empty." }
        val list = ArrayList<Permission>()
        for ((_, next) in permissions) if (next.isSub(formattedContext)) list.add(next)
        return list
    }

    /**
     * TODO: Document.
     */
    fun getClosest(context: String): Permission? {
        val formattedContext = context.toLowerCase().trim()
        require(formattedContext.isNotEmpty()) { "The context is empty." }
        var closest: Permission? = null
        for ((_, next) in permissions) {
            if (next.equals(formattedContext)) closest = next
            else if (next.isSuper(formattedContext)) {
                if (closest != null) {
                    if (closest.isSuper(next)) closest = next
                } else {
                    closest = next
                }
            }
        }
        return closest
    }

    /**
     * TODO: Document.
     */
    fun getExplicit(context: String): Permission? {
        val formattedContext = context.toLowerCase().trim()
        require(formattedContext.isNotEmpty()) { "The context is empty." }
        return permissions[formattedContext]
    }

    /**
     * TODO: Document.
     */
    open fun has(context: String): Boolean = getClosest(context)?.flag ?: false

    companion object {

        /**
         * TODO: Document.
         */
        fun isAnyGranted(permissions: List<Permission>): Boolean {
            for (node in permissions) if (node.flag) return true
            return false
        }

        /**
         * TODO: Document.
         */
        fun getGrantedNodes(permissions: List<Permission>): List<Permission> {
            val list = ArrayList<Permission>()
            for (node in permissions) if (node.flag) list.add(node)
            return list
        }
    }
}
