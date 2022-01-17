package jab.sledgehammer.util

import java.lang.reflect.Method
import java.util.*

object ClassUtil {

    fun getAllDeclaredMethods(clazz: Class<*>): List<Method> {
        val methods = ArrayList<Method>()
        var c: Class<*>? = clazz
        while (c != null) {
            methods.addAll(listOf(*c.declaredMethods))
            c = c.superclass
        }
        return methods
    }

    fun isSubClass(subClass: Class<*>, superClass: Class<*>): Boolean = superClass.isAssignableFrom(subClass)
}
