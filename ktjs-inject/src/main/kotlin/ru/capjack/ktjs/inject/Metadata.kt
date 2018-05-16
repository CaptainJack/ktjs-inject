package ru.capjack.ktjs.inject

import kotlin.reflect.KClass

internal object Metadata {
	
	fun <T : Any> getInject(type: KClass<out T>): InjectMetadata<T> {
		return getMetadata(type, "inject")
	}
	
	fun getInjectProxy(type: KClass<out Any>): Array<Array<Any>> {
		return getMetadata(type, "injectProxy")
	}
	
	private fun <D> getMetadata(type: KClass<out Any>, section: String): D {
		val value = type.js.asDynamic()["\$metadata$"][section]
			?: throw IllegalArgumentException("Type ${type.simpleName} is not injectable")
		
		return value.unsafeCast<D>()
	}
}

internal external interface InjectMetadata<T : Any> {
	val args: Array<Any>
	fun create(args: Array<Any>): T
}

