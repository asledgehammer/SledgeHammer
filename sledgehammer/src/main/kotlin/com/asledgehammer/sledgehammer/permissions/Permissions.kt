@file:Suppress("unused")

package com.asledgehammer.sledgehammer.permissions

import com.asledgehammer.framework.cfg.CFGSection
import com.asledgehammer.framework.cfg.YamlFile
import com.asledgehammer.sledgehammer.Sledgehammer
import java.io.File
import java.util.*

/**
 * **Permissions** TODO: Document.
 *
 * @author Jab
 */
object Permissions {

  private val groupsById = HashMap<UUID, PermissionGroup>()
  private val groupsByName = HashMap<String, PermissionGroup>()
  private val usersById = HashMap<UUID, PermissionUser>()
  private val usersByName = HashMap<String, PermissionUser>()

  /** TODO: Document. */
  @JvmStatic
  fun loadYAML(file: File) {

    val groupParentIds = HashMap<UUID, UUID>()

    fun loadPermissions(cfg: CFGSection): List<Permission> {
      val list = ArrayList<Permission>()
      val permissions = cfg.getSection("permissions")
      for (key in permissions.allKeys) {
        if (!key.startsWith(CFGSection.SEPARATOR)) {
          Sledgehammer.logError("Invalid permission context syntax: $key")
          continue
        }
        if (!permissions.isBoolean(key)) {
          Sledgehammer.logError("Invalid permission flag: ${permissions.get(key)}")
          continue
        }
        list.add(Permission(key, permissions.getBoolean(key)))
      }
      return list
    }

    /** TODO: Document. */
    fun loadUsers(cfg: CFGSection) {
      for ((idString, cfgUser) in cfg.sections) {
        val id: UUID
        try {
          id = UUID.fromString(idString)
        } catch (e: IllegalArgumentException) {
          Sledgehammer.logError("Invalid UUID for permission user: $idString")
          continue
        }
        require(cfgUser.contains("name")) {
          "Permission user does not have a name."
        }
        require(cfgUser.isString("name")) {
          "Permission user does not have a valid name. (${cfgUser.get("name")})"
        }
        val name = cfgUser.getString("name")
        val user = PermissionUser(id, name)
        if (cfgUser.isSection("permissions")) {
          val permissions = loadPermissions(cfg.getSection("permissions"))
          user.set(permissions)
        }
        usersById[user.id] = user
        usersByName[user.name] = user
      }
    }

    /** TODO: Document. */
    fun loadGroups(cfg: CFGSection) {
      for ((idString, cfgGroup) in cfg.sections) {
        val id: UUID
        try {
          id = UUID.fromString(idString)
        } catch (e: IllegalArgumentException) {
          Sledgehammer.logError("Invalid UUID for permission group: $idString")
          continue
        }
        require(cfgGroup.contains("name")) {
          "Permission group does not have a name."
        }
        require(cfgGroup.isString("name")) {
          "Permission group does not have a valid name. (${cfgGroup.get("name")})"
        }
        val name = cfgGroup.getString("name")
        val group = PermissionGroup(id, name)
        if (cfgGroup.isSection("permissions")) {
          val permissions = loadPermissions(cfg.getSection("permissions"))
          group.set(permissions)
        }

        if (cfgGroup.isList("members")) {
          for (userIdString in cfgGroup.getStringList("members")) {
            val userId: UUID
            try {
              userId = UUID.fromString(userIdString)
            } catch (e: IllegalArgumentException) {
              Sledgehammer.logError("Invalid UUID for group member: (group: ${group.name}, id: $userIdString)")
              continue
            }
            val user = usersById[userId]
            if (user == null) {
              Sledgehammer.logError("User does not exist: (group: ${group.name}, id: $userId)")
              continue
            }
            group.addMember(user)
          }
        }

        if (cfgGroup.isString("parent")) {
          val parentIdString = cfgGroup.getString("parent")
          val parentId: UUID
          try {
            parentId = UUID.fromString(parentIdString)
          } catch (e: IllegalArgumentException) {
            System.err.println("Invalid UUID for group parent: (group: ${group.name}, id: $parentIdString)")
            continue
          }
          groupParentIds[group.id] = parentId
        }

        groupsById[group.id] = group
        groupsByName[group.name] = group

      }
    }

    fun pairGroups() {
      for ((groupId, parentId) in groupParentIds) {
        val group = groupsById[groupId]!!
        val parent = groupsById[parentId]
        if (parent == null) {
          System.err.println("Parent for group \"$group\" doesn't exist: $parentId")
          continue
        }
        group.parent = parent
      }
    }

    val config = YamlFile(file)
    config.read()
    if (config.isSection("users")) loadUsers(config.getSection("users"))
    if (config.isSection("groups")) loadGroups(config.getSection("groups"))
    if (groupsById.isNotEmpty() && groupParentIds.isNotEmpty()) {
      pairGroups()
    }
  }

  /** TODO: Document. */
  @JvmStatic
  fun saveYAML(file: File) {

    fun savePermissions(cfg: CFGSection, collection: PermissionCollection) {
      for ((_, permission) in collection.permissions) {
        cfg.set(permission.context, permission.flag)
      }
    }

    fun saveGroup(cfg: CFGSection, group: PermissionGroup) {
      cfg.set("name", group.name)
      if (group.parent != null) cfg.set("parent", group.parent!!.id.toString())
      if (group.hasMembers()) {
        val list = ArrayList<String>()
        for (member in group.members) {
          list.add(member.id.toString())
        }
        cfg.set("members", list)
      }
      savePermissions(cfg.createSection("permissions"), group)
    }

    fun saveUser(cfg: CFGSection, user: PermissionUser) {
      cfg.set("name", user.name)
      savePermissions(cfg.createSection("permissions"), user)
    }

    fun saveGroups(cfg: CFGSection) {
      for ((_, group) in groupsById) saveGroup(cfg.createSection(group.id.toString()), group)
    }

    fun saveUsers(cfg: CFGSection) {
      for ((_, user) in usersById) saveUser(cfg.createSection(user.id.toString()), user)
    }

    val config = YamlFile(file)
    config.read()
    saveGroups(config.createSection("groups"))
    saveUsers(config.createSection("users"))
  }

  /** TODO: Document. */
  @JvmStatic
  fun addGroup(group: PermissionGroup) {
    require(groupsById[group.id] == null) { "Permission group already exists: $group" }
    groupsById[group.id] = group
    groupsByName[group.name] = group
  }

  /** TODO: Document. */
  @JvmStatic
  fun getGroup(id: UUID): PermissionGroup {
    val group = groupsById[id]
    requireNotNull(group) { "Permission group not found: $id" }
    return group
  }

  /** TODO: Document. */
  @JvmStatic
  fun getGroup(name: String): PermissionGroup {
    val formattedName = name.toLowerCase().trim()
    val group = groupsByName[formattedName]
    requireNotNull(group) { "Permission group not found: $formattedName" }
    return group
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeGroup(group: PermissionGroup) {
    groupsById.remove(group.id)
    groupsByName.remove(group.name)
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeGroup(id: UUID) {
    val group = groupsById.remove(id)
    requireNotNull(group) { "Permission group not found: $id" }
    removeGroup(group)
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeGroup(name: String) {
    val formattedName = name.toLowerCase().trim()
    val group = groupsByName.remove(formattedName)
    requireNotNull(group) { "Permission group not found: $formattedName" }
    removeGroup(group)
  }

  /** TODO: Document. */
  @JvmStatic
  fun addUser(user: PermissionUser) {
    require(usersById[user.id] == null) { "Permission user already exists: $user" }
    usersById[user.id] = user
    usersByName[user.name] = user
  }

  /** TODO: Document. */
  @JvmStatic
  fun getUser(id: UUID): PermissionUser {
    val user = usersById[id]
    requireNotNull(user) { "Permission user not found: $id" }
    return user
  }

  /** TODO: Document. */
  @JvmStatic
  fun getUser(name: String): PermissionUser {
    val formattedName = name.toLowerCase().trim()
    val user = usersByName[formattedName]
    requireNotNull(user) { "Permission user not found: $formattedName" }
    return user
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeUser(user: PermissionUser) {
    usersById.remove(user.id)
    usersByName.remove(user.name)
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeUser(id: UUID) {
    val user = usersById.remove(id)
    requireNotNull(user) { "Permission user not found: $id" }
    removeUser(user)
  }

  /** TODO: Document. */
  @JvmStatic
  fun removeUser(name: String) {
    val formattedName = name.toLowerCase().trim()
    val user = usersByName.remove(formattedName)
    requireNotNull(user) { "Permission user not found: $formattedName" }
    removeUser(user)
  }
}
