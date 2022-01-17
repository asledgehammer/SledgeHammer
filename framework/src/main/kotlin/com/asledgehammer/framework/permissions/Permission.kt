@file:Suppress("unused")

package com.asledgehammer.framework.permissions

/**
 * TODO: Document.
 *
 * @author Jab
 *
 * @property context
 * @property flag
 */
class Permission(val context: String, var flag: Boolean) {

    override fun equals(other: Any?): Boolean = other is Permission && other.context == context && other.flag == flag
    override fun hashCode(): Int = 31 * context.hashCode() + flag.hashCode()
    override fun toString(): String = "$context$VALUE_SEPARATOR${if (flag) "1" else "0"}"

    /**
     * TODO: Document.
     */
    fun isSuper(permission: Permission): Boolean = isSuper(permission.context)

    /**
     * TODO: Document.
     */
    fun isSuper(context: String): Boolean = context.contains(this.context, true)

    /**
     * TODO: Document.
     */
    fun isSub(permission: Permission): Boolean = isSub(permission.context)

    /**
     * TODO: Document.
     */
    fun isSub(context: String): Boolean = this.context.contains(context, true)

    /**
     * TODO: Document.
     */
    fun equals(raw: String): Boolean = toString().equals(raw, true)

    companion object {

        /**
         * TODO: Document.
         */
        const val NODE_SEPARATOR = '.'

        /**
         * TODO: Document.
         */
        const val VALUE_SEPARATOR = ':'

        /**
         * TODO: Document.
         */
        fun fromString(raw: String): Permission {
            require(raw.contains(VALUE_SEPARATOR)) {
                "The raw string does not contain a value separator ($VALUE_SEPARATOR): $raw"
            }
            val split = raw.split(VALUE_SEPARATOR)
            require(split.size == 2) {
                "The raw string contains more than one value separator ($VALUE_SEPARATOR): $raw"
            }
            return Permission(split[0].toLowerCase().trim(), split[1].trim().toBoolean())
        }
    }
}
