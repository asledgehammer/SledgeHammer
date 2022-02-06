@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.asledgehammer.sledgehammer.permissions

import java.util.*

/**
 * **PermissionGroup** TODO: Document.
 *
 * @author Jab
 */
class PermissionGroup(id: UUID = UUID.randomUUID(), name: String) : PermissionCollection(id, name) {

  /** TODO: Document. */
  val members: List<PermissionUser> get() = users.values.toList()

  /** TODO: Document. */
  val parentId: UUID? get() = parent?.id

  /** TODO: Document. */
  var parent: PermissionGroup? = null

  private val users = HashMap<UUID, PermissionUser>()

  override fun has(context: String): Boolean {
    var returned = false
    var parentSpecific: Permission? = null
    if (parent != null) {
      parentSpecific = parent!!.get(context)
      if (parentSpecific != null) returned = parentSpecific.flag
    }
    val permissionSpecific: Permission? = get(context)
    if (permissionSpecific != null) {
      returned = if (parentSpecific != null) {
        if (parentSpecific == permissionSpecific || parentSpecific.isSuper(permissionSpecific)) {
          permissionSpecific.flag
        } else parentSpecific.flag
      } else permissionSpecific.flag
    } else {
      if (allPermissions.isNotEmpty()) {
        var permission: Permission? = null
        for (permissionNodeNext in allPermissions) {
          if (permissionNodeNext.isSub(context)
            && (permission == null || permission.isSub(permissionNodeNext))
          ) {
            permission = permissionNodeNext
          }
        }
        if (permission != null) returned = permission.flag
      }
    }
    return returned
  }

  override fun getAllSub(context: String): List<Permission> {
    val superContext = context.toLowerCase().trim()
    val list = ArrayList<Permission>()
    if (parent != null) list.addAll(parent!!.getAllSub(superContext))
    for ((_, permission) in permissions) {
      if (permission.isSub(superContext)) {
        if (list.contains(permission)) {
          list.remove(permission)
          list.add(permission)
        }
      }
    }
    return list
  }

  override fun equals(other: Any?): Boolean = other is PermissionGroup && other.id == id
  override fun hashCode(): Int = 31 * id.hashCode() + name.hashCode()
  override fun toString(): String = "PermissionGroup(name=$name, id=$id)"

  /** TODO: Document. */
  val allPermissions: List<Permission>
    get() {
      val list = ArrayList<Permission>()
      if (parent != null) list.addAll(parent!!.allPermissions)
      if (list.isEmpty()) return ArrayList(permissions.values)

      for ((_, node) in permissions) {
        for (nodeParent in list) {
          if (nodeParent == node) {
            list.remove(nodeParent)
            list.add(node)
          }
        }
      }
      return list
    }

  /** TODO: Document. */
  fun isChildOf(other: PermissionGroup): Boolean = parent != null && (parent == other || parent!!.isChildOf(other))

  /** TODO: Document. */
  fun hasMember(member: PermissionUser): Boolean = users.containsKey(member.id)

  /** TODO: Document. */
  fun addMember(user: PermissionUser) {
    users[user.id] = user
    user.group = this
  }

  /** TODO: Document. */
  fun removeMember(user: PermissionUser) {
    users.remove(user.groupId)
    user.group = null
  }

  /** TODO: Document. */
  fun hasMembers(): Boolean = users.isNotEmpty()
}
