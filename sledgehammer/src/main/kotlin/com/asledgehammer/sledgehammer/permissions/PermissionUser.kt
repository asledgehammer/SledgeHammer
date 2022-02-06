package com.asledgehammer.sledgehammer.permissions

import java.util.*

/**
 * **PermissionUser** TODO: Document.
 *
 * @author Jab
 *
 * @param id
 * @param name
 * @property group
 */
class PermissionUser(id: UUID = UUID.randomUUID(), name: String, var group: PermissionGroup? = null) :
  PermissionCollection(id, name) {

  /** TODO: Document. */
  val groupId: UUID? get() = group?.id

  override fun has(context: String): Boolean {
    val permissionGroup: Permission? = group?.getClosest(context)
    val permissionUser = getClosest(context)
    if (permissionUser != null) {
      return if (permissionGroup != null) {
        if (permissionGroup == permissionUser || permissionUser.isSub(permissionGroup)) permissionUser.flag
        else permissionGroup.flag
      } else permissionUser.flag
    } else if (permissionGroup != null) return permissionGroup.flag
    return false
  }

  override fun toString(): String = "PermissionUser(name=$name, id=$id)"
}
