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
		val metadata = Metadata.getProxy(type)
		methods = metadata.map {
			Method(
				it[0].unsafeCast<String>(),
				it[1].unsafeCast<String>(),
				it[2].unsafeCast<KClass<Any>>(),
				it[3].unsafeCast<Int>()
			)
		}
	}
	
	override fun <T : Any> provides(method: String, implementation: KClass<T>): ProxyBinder {
		(methods.find { it.name == method } ?: throw IllegalArgumentException("Method $method not found in type ${type.simpleName}"))
			.implementation = implementation
		return this
	}
	
	override fun <T : Any, I : T> provides(type: KClass<T>, implementation: KClass<I>): ProxyBinder {
		(methods.find { it.returnType == type } ?: throw IllegalArgumentException("Method for return type ${type.simpleName} not found in type ${type.simpleName}"))
			.implementation = implementation
		return this
	}
	
	fun build(injector: InjectorImpl): T {
		val o: dynamic = jso()
		
		methods.forEach {
			val t = it.implementation
			o[it.kotlinName] = if (it.argAmount == 0) {
				{ injector.create(t) }
			} else {
				{ injector.create(t, js("Array.prototype.slice.call(arguments)").unsafeCast<Array<Any>>()) }
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