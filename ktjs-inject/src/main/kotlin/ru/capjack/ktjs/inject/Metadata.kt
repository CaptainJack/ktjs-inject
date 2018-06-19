package ru.capjack.ktjs.inject

import kotlin.reflect.KClass

internal object Metadata {
	
	fun <T : Any> getInject(type: KClass<out T>): MetadataInject<T> {
		return get(type, "inject")
	}
	
	fun getProxy(type: KClass<out Any>): Array<Array<Any>> {
		return get(type, "injectProxy")
	}
	
	fun <T : Any> getBind(type: KClass<out T>): MetadataBind? {
		return find(type, "injectBind")
	}
	
	private fun <D> find(type: KClass<out Any>, section: String): D? {
		return type.js.asDynamic()["\$metadata$"][section].unsafeCast<D?>()
	}
	
	private fun <D> get(type: KClass<out Any>, section: String): D {
		return find(type, section) ?: throw IllegalArgumentException("Type ${type.simpleName} is not injectable")
	}
}

internal external interface MetadataInject<T : Any> {
	val args: Array<Any>
	fun create(args: Array<Any>): T
}

internal external interface MetadataBind {
	val implementation: Any
	val multiple: Boolean
}

