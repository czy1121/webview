package me.reezy.cosmo.jsbridge.internal

internal object JSScript {

    fun gen(bridgeName: String, modules: Collection<JSModule>): String {
        val modulesScript = modules.joinToString("") { "$bridgeName.${it.instance.name}={${it.methodsScript}};" }
        return "(function(){if($bridgeName.onCallback)return;var _bridgeName='$bridgeName', _bridge=$bridgeName;$invokeScript;$modulesScript;$bridgeName.onCallback=_callback;})()"
    }

    private val JSModule.methodsScript: String
        get() = methods.values.joinToString(",") {
            "${it.name}:function(){return _invoke('${it.module.name}', '${it.name}', arguments, ${it.hasReturn})}"
        }

    private val invokeScript = """ 
	var _id = 1, _callbacks = {}; 
	function _value(name, value, ref, callbacks) {
        if (typeof value === 'function') {
            callbacks[name] = value;
            return ref + name;
        }

        if (typeof value === 'object') {
            for (var k in value) {
                if (typeof value[k] === 'function') {
                    var cbName = name + '_' + k;
                    callbacks[cbName] = value[k];
                    value[k] = ref + cbName;
                }
            }
        }
        return value;
	}

	function _invoke(moduleName, methodName, methodArgs, hasReturn) {
	    var reqId = _id++, bucket = moduleName + '.' + methodName, ref = _bridgeName + "#" + bucket + "#" + reqId + "#";

	    var arguments = [], callbacks = {};
        for (var i in methodArgs) {
            arguments.push(_value('A' + i, methodArgs[i], ref, callbacks));
        }
        if (Object.keys(callbacks).length > 0) {
            if(!_callbacks[bucket]) _callbacks[bucket] = {};
            _callbacks[bucket][reqId] = callbacks;
        }

        var result = JSON.parse(_bridge.call(moduleName, methodName, JSON.stringify(arguments)));
        if (result.success) {
            if (hasReturn) {
                return result.data;
            }
        } else {
            console.log(result.message);
        }
	}

    function _callback(bucket, reqId, name, args) {
        try {
            _callbacks[bucket][reqId][name](...args);
        } catch (e) {
        
        }
        try {
            delete _callbacks[bucket][reqId];
        } catch (e) {
        
        }
    }
    """.replace(Regex("[\n\t\r]+"), " ").replace(Regex("[\\s]+"), " ")
}