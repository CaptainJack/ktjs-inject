package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.inject.bindings.Binding
import kotlin.reflect.KClass

internal class InjectorImpl() : Injector {
	
	private val bindings: MutableMap<KClass<*>, Binding<*>> = mutableMapOf()
	private val namedBindings: MutableMap<TypedName<*>, Binding<*>> = mutableMapOf()
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(type: KClass<T>): T {
		if (type == Injector::class) {
			return this as T
		}
		val binding = bindings[type] ?: throw NoSuchElementException("Binding for class \"${type.simpleName}\" is not defined")
		return binding.get() as T
	}
	
	override fun <T : Any> KClass<T>.unaryPlus(): T {
		return get(this)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(name: TypedName<T>): T {
		val binding = namedBindings[name] ?: throw NoSuchElementException("Binding for name $name is not defined")
		return binding.get() as T
	}
	
	override fun <T : Any> TypedName<T>.unaryPlus(): T {
		return get(this)
	}
	
	internal fun <T : Any> containsBinding(type: KClass<T>): Boolean {
		return bindings.containsKey(type)
	}
	
	internal fun <T : Any> containsBinding(name: TypedName<T>): Boolean {
		return namedBindings.containsKey(name)
	}
	
	internal fun <T : Any> setBinding(type: KClass<T>, binding: Binding<T>) {
		bindings[type] = binding
	}
	
	internal fun <T : Any> setBinding(name: TypedName<T>, binding: Binding<T>) {
		namedBindings[name] = binding
	}
}