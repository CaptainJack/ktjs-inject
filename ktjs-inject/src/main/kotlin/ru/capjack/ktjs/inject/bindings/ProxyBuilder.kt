package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.common.js.jso
import ru.capjack.ktjs.inject.InjectorImpl
import ru.capjack.ktjs.inject.Metadata
import ru.capjack.ktjs.inject.ProxyBinder
import kotlin.reflect.KClass

internal class ProxyBuilder<T : Any>(
	private val type: KClass<T>
) : ProxyBinder {
	
	private var methods: List<Method>
	
	init {
		methods = Metadata.getInjectProxy(type).map {
			Method(
				it[0].unsafeCast<String>(),
				it[1].unsafeCast<String>(),
				it[2].unsafeCast<KClass<Any>>(),
				it[3].unsafeCast<Int>()
			)
		}
	}
	
	override fun <T : Any> bind(method: String, implementation: KClass<T>) {
		(methods.find { it.name == method } ?: throw IllegalArgumentException("Method $method not found in type ${type.simpleName}"))
			.implementation = implementation
	}
	
	override fun <T : Any, I : T> bind(type: KClass<T>, implementation: KClass<I>) {
		(methods.find { it.returnType == type } ?: throw IllegalArgumentException("Method for return type ${type.simpleName} not found in type ${type.simpleName}"))
			.implementation = implementation
	}
	
	fun build(injector: InjectorImpl): T {
		val o: dynamic = jso()
		
		methods.forEach {
			o[it.kotlinName] =
				if (it.argAmount == 0) {
					injector.create(it.implementation)
				}
				else {
					injector.create(it.implementation, js("Array.prototype.slice.call(arguments)").unsafeCast<Array<Any>>())
				}
		}
		
		return o.unsafeCast<T>()
	}
	
	private class Method(
		val name: String,
		val kotlinName: String,
		val returnType: KClass<out Any>,
		val argAmount: Int,
		var implementation: KClass<out Any> = returnType
	)
}