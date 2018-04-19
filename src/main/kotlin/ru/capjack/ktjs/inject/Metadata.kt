package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.common.mapToArray
import kotlin.reflect.KClass

internal object Metadata {
	
	fun <T : Any, D> getMetadata(type: KClass<T>, section: String): D {
		val value = type.js.asDynamic()["\$metadata$"][section]
			?: throw IllegalArgumentException("Type ${type.simpleName} is not injectable")
		
		return value.unsafeCast<D>()
	}
	
	fun <T : Any> getMetadataInject(type: KClass<T>): InjectMetadata<T> {
		return getMetadata(type, "inject")
	}
	
	fun <T : Any> getMetadataInjectProxy(type: KClass<T>): Array<Array<*>> {
		return getMetadata(type, "injectProxy")
	}
	
	fun getInstance(injector: Injector, injectType: Any): Any {
		return if (injectType is Array<*>) {
			injector[TypedName(injectType[0].unsafeCast<KClass<*>>(), injectType[1].unsafeCast<String>())]
		}
		else {
			injector[injectType.unsafeCast<KClass<*>>()]
		}
	}
	
	fun getInstances(injector: Injector, injectTypes: Array<Any>): Array<Any> {
		return injectTypes.mapToArray { getInstance(injector, it) }
	}
}

internal external interface InjectMetadata<out T : Any> {
	val types: Array<Any>
	
	fun create(args: Array<Any>): T
}

