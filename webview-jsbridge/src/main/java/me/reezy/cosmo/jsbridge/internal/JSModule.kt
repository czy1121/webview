package me.reezy.cosmo.jsbridge.internal

import me.reezy.cosmo.jsbridge.JSBridgeMethod
import me.reezy.cosmo.jsbridge.JSBridgeModule
import me.reezy.cosmo.jsbridge.argument.JSCallback
import me.reezy.cosmo.jsbridge.argument.JSMap
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Modifier

internal class JSModule(val instance: JSBridgeModule) {
    val methods: Map<String, JSMethod> = getMethods(instance)

    private fun getMethods(module: JSBridgeModule): Map<String, JSMethod> {
        val methods = mutableMapOf<String, JSMethod>()
        module.javaClass.declaredMethods.forEach {
            val modifiers = it.modifiers
            if (Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers)) {
                return@forEach
            }
            val annotation = it.getAnnotation(JSBridgeMethod::class.java) ?: return@forEach

            val name = if (annotation.name.isEmpty()) it.name else annotation.name
            val mainThread = annotation.mainThread
            val hasReturn = "void" != it.returnType.name

//            var hasContext = false
            val parameters = it.parameterTypes.toList()
            val argumentTypes: List<Int> = when {
                parameters.isEmpty() -> emptyList()
//                parameters[0] == JSContext::class.java -> {
//                    hasContext = true
//                    parameters.subList(1, parameters.size).map(this::getType)
//                }
                else -> parameters.map(this::getType)
            }


            methods[name] = JSMethod(module, it, name, hasReturn, argumentTypes, mainThread)
        }
        return methods
    }

    private fun getType(clazz: Class<*>): Int = when (clazz) {
        Boolean::class.java -> JSType.TYPE_BOOL
        Int::class.java -> JSType.TYPE_INT
        Long::class.java -> JSType.TYPE_LONG
        Float::class.java -> JSType.TYPE_FLOAT
        Double::class.java -> JSType.TYPE_DOUBLE
        String::class.java -> JSType.TYPE_STRING
        JSONObject::class.java -> JSType.TYPE_OBJECT
        JSONArray::class.java -> JSType.TYPE_ARRAY
        JSMap::class.java -> JSType.TYPE_MAP
        JSCallback::class.java -> JSType.TYPE_CALLBACK
        else -> JSType.TYPE_UNDEFINED
    }
}