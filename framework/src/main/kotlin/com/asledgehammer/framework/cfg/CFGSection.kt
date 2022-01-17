@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.asledgehammer.framework.cfg

import java.util.*

/**
 * **ConfigSection** is a hierarchical, mutable collection of standard Java primitives and objects.
 *
 * ConfigSection implements a strict API when checking, querying, and setting values. Utilizing internal checks ensures
 * better coding practices alongside both Kotlin and Java usage of the library.
 *
 * @author Jab
 *
 * @property name The name of the section, as to be referenced in the hierarchy when querying.
 */
open class CFGSection internal constructor(val name: String) {

    /**
     * The parent of the section.
     */
    var parent: CFGSection? = null
        /**
         * @param value The section to set.
         *
         * @throws CyclicException Thrown if the value is a child or itself.
         */
        set(value) {
            if (value != null) {
                if (value == this) {
                    throw CyclicException("Cannot set parent as self.")
                } else if (value.isChildOf(this)) {
                    throw CyclicException("Parent section is a child of the section.")
                }
            }
            field = value
        }

    /**
     * Returns a immutable collection of both children keys and field keys in the section.
     */
    val allKeys: List<String>
        get() {
            val list = ArrayList(children.keys)
            list.addAll(fields.keys)
            return Collections.unmodifiableList(list)
        }

    /**
     * Returns a immutable collection of children keys in the section.
     */
    val childKeys: List<String>
        get() = Collections.unmodifiableList(ArrayList(children.keys))

    /**
     * Returns a immutable collection of field keys in the section.
     */
    val fieldKeys: List<String>
        get() = Collections.unmodifiableList(ArrayList(fields.keys))

    /**
     * An immutable map of the children of the section.
     */
    val sections: Map<String, CFGSection> get() = Collections.unmodifiableMap(HashMap(children))

    /**
     * If the section does not have a parent, ***true*** is returned.
     */
    val orphaned: Boolean get() = this !is CFGFile && parent == null

    internal val children = HashMap<String, CFGSection>()

    internal val fields = HashMap<String, Any>()

    internal constructor() : this("")

    /**
     * @param query The query to test.
     *
     * @return Returns ***true*** if the query resolves as a non-null value.
     */
    fun contains(query: String): Boolean {
        require(query.isNotEmpty()) {
            "The query is empty."
        }
        if (query.contains(SEPARATOR)) {
            val split = query.split(SEPARATOR)
            val childQuery = split[0].toLowerCase().trim()
            val child = children[childQuery] ?: return false
            val rebuiltQuery = StringBuilder()
            for (index in 1..split.lastIndex) {
                rebuiltQuery.append(if (rebuiltQuery.isEmpty()) split[index] else "$SEPARATOR${split[index]}")
            }
            return child.contains(rebuiltQuery.toString())
        }
        return fields[query] != null || children[query] != null
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns the resolved query.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve.
     */
    fun get(query: String): Any {
        require(query.isNotEmpty()) {
            "The query is empty."
        }
        if (query.contains(SEPARATOR)) {
            val split = query.split(SEPARATOR)
            val childQuery = split[0].toLowerCase().trim()
            val child = children[childQuery] ?: throw UnresolvedException(childQuery)
            val rebuiltQuery = StringBuilder()
            for (index in 1..split.lastIndex) {
                rebuiltQuery.append(if (rebuiltQuery.isEmpty()) split[index] else "$SEPARATOR${split[index]}")
            }
            return child.get(rebuiltQuery.toString())
        }
        return fields[query] ?: children[query] ?: throw UnresolvedException(query)
    }

    /**
     * Sets a value for the query.
     *
     * If the value is stored directly in this section, simply use the name of the value. If the value is stored in a
     * child section, make sure to delimit each child in the hierarchy with a period. **E.G:** **child.value**
     *
     * **NOTE:** If the section does not exist, make sure to create it before setting the value.
     *
     * @param query The query to resolve.
     * @param value The value to set for the query.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if a sub-section in the query does not exist.
     */
    fun set(query: String, value: Any?) {
        require(query.isNotEmpty()) {
            "The query is empty."
        }
        if (query.contains(SEPARATOR)) {
            val split = query.split(SEPARATOR)
            val childQuery = split[0].toLowerCase().trim()
            val child = children[childQuery] ?: throw UnresolvedException(childQuery)
            val rebuiltQuery = StringBuilder()
            for (index in 1..split.lastIndex) {
                rebuiltQuery.append(if (rebuiltQuery.isEmpty()) split[index] else "$SEPARATOR${split[index]}")
            }
            child.set(rebuiltQuery.toString(), value)
        } else {
            setLocal(query, value)
        }
    }

    /**
     * This is a cleaner solution to isolate discovery-query code from application of values.
     */
    private fun setLocal(query: String, value: Any?) {
        val lQuery = query.toLowerCase().trim()
        if (value is CFGSection) {
            if (isChildOf(value)) throw CyclicException("Cannot set parent as child.")
            children[lQuery] = value
        } else {
            if (value != null) {
                fields[lQuery] = value
            } else {
                children.remove(lQuery)
                fields.remove(lQuery)
            }
        }
    }

    /**
     * Creates a child section and appends to the invoked section.
     *
     * @param name The name of the new section.
     *
     * @return Returns the new section.
     *
     * @throws IllegalArgumentException Thrown if the name is empty.
     * @throws FieldExistsException Thrown if the name is already used by a section or field.
     */
    fun createSection(name: String): CFGSection {
        require(name.isNotEmpty()) {
            "The name given is empty."
        }
        require(children[name] == null) {
            "A section already exists with the name: $name"
        }
        if (contains(name)) {
            if (isSection(name)) {
                throw FieldExistsException("The section \"$name\" exists in \"${this.name}\".")
            } else {
                throw FieldExistsException("The field \"$name\" exists in \"${this.name}\".")
            }
        }

        val section = CFGSection(name)
        section.parent = this
        children[name] = section
        return section
    }

    /**
     * @return Returns a deep-copy of the section and all children with their fields as a map.
     * Sections are Map<String, Object>. Values are generic.
     */
    fun toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        if (children.isNotEmpty()) for ((key, child) in children) map[key] = child.toMap()
        if (fields.isNotEmpty()) for ((key, value) in fields) map[key] = value
        return map
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns a List of Long values.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     * @throws NumberFormatException Thrown if the resolved List contains a value that is not a Long.
     */
    fun getLongList(query: String): List<Long> {
        val rawList = getList(query)
        val list = ArrayList<Long>()
        for (next in rawList) list.add(next.toString().toLong())
        return list
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns a list of double values.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     * @throws NumberFormatException Thrown if the resolved list contains a value that is not a double.
     */
    fun getDoubleList(query: String): List<Double> {
        val rawList = getList(query)
        val list = ArrayList<Double>()
        for (next in rawList) list.add(next.toString().toDouble())
        return list
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns a list of integer values.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     * @throws NumberFormatException Thrown if the resolved list contains a value that is not a integer.
     */
    fun getIntList(query: String): List<Int> {
        val rawList = getList(query)
        val list = ArrayList<Int>()
        for (next in rawList) list.add(next.toString().toInt())
        return list
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns a list of boolean values.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     * @throws NumberFormatException Thrown if the resolved list contains a value that is not a boolean.
     */
    fun getBooleanList(query: String): List<Boolean> {
        val rawList = getList(query)
        val list = ArrayList<Boolean>()
        for (next in rawList) list.add(next.toString().toBoolean())
        return list
    }

    /**
     * @param query The query to resolve.
     *
     * @return Returns a list of strings.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getStringList(query: String): List<String> {
        val rawList = getList(query)
        val list = ArrayList<String>()
        for (next in rawList) list.add(next.toString())
        return list
    }

    /**
     * @param section The section to test.
     *
     * @return Returns ***true*** if this section is a child of the given section.
     */
    fun isChildOf(section: CFGSection): Boolean {
        if (this.parent == null) return false
        if (this.parent == section) return true
        return this.parent!!.isChildOf(section)
    }

    /**
     * Tests a query's type.
     *
     * @param query The query to test.
     * @param clazz The class of the type to test.
     *
     * @return Returns true if the resolved query is the type given. If the query does not resolve, false is returned.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun <E> isType(query: String, clazz: Class<E>): Boolean =
        contains(query) && clazz.isAssignableFrom(get(query)::class.java)

    /**
     * Resolves a query into a type-casted value.
     *
     * @param query The query to resolve.
     * @param clazz The type of class to cast.
     *
     * @return Returns the resolved query.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    @Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
    fun <E> get(query: String, clazz: Class<E>): E = get(query) as E

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a section.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isSection(query: String): Boolean = isType(query, CFGSection::class.java)

    /**
     * Resolves a query as a section.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved section.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getSection(query: String): CFGSection = get(query, CFGSection::class.java)

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a string.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isString(query: String): Boolean = isType(query, String::class.java)

    /**
     * Resolves a query into a string.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved string.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getString(query: String): String = get(query).toString()

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a boolean.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isBoolean(query: String): Boolean = isType(query, Boolean::class.java)

    /**
     * Resolves a query into a boolean value.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved boolean value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getBoolean(query: String): Boolean = get(query, Boolean::class.java)

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a integer value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isInt(query: String): Boolean = isType(query, Int::class.java)

    /**
     * Resolves a query into a integer value.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved integer value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getInt(query: String): Int = get(query, Int::class.java)

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a double value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isDouble(query: String): Boolean = isType(query, Double::class.java)

    /**
     * Resolves a query into a double value.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved double value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getDouble(query: String): Double = get(query, Double::class.java)

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a long value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isLong(query: String): Boolean = isType(query, Long::class.java)

    /**
     * Resolves a query into a long value.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved long value.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getLong(query: String): Long = get(query, Long::class.java)

    /**
     * @param query The query to test.
     *
     * @return Returns true if the query resolves and the resolved value is a list.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     */
    fun isList(query: String): Boolean = isType(query, List::class.java)

    /**
     * Resolves a query into a generic list.
     *
     * @param query The query to resolve.
     *
     * @return Returns the resolved list.
     *
     * @throws IllegalArgumentException Thrown if the query is empty.
     * @throws UnresolvedException Thrown if the query fails to resolve a value.
     * @throws ClassCastException Thrown if the resolved value is not a list.
     */
    fun getList(query: String): List<*> = get(query, List::class.java)

    companion object {

        /**
         * The delimiter for cross-section queries.
         */
        const val SEPARATOR = '.'
    }
}
