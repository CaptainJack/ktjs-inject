package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.common.cutLast
import ru.capjack.ktjs.inject.bindings.Binding
import kotlin.reflect.KClass

internal class InjectorImpl() : Injector {
	
	private val typeBindings: MutableMap<KClass<out Any>, Binding<Any>> = mutableMapOf()
	private val namedBindings: MutableMap<TypedName<out Any>, Binding<Any>> = mutableMapOf()
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(type: KClass<T>): T {
		return typeBindings[type].let {
			if (it == null) create(type)
			else it.get() as T
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> get(name: TypedName<T>): T {
		val binding = namedBindings[name] ?: throw NoSuchElementException("Binding for name $name is not defined")
		return binding.get() as T
	}
	
	internal fun <T : Any> contains(type: KClass<T>): Boolean {
		return typeBindings.containsKey(type)
	}
	
	internal fun <T : Any> contains(name: TypedName<T>): Boolean {
		return namedBindings.containsKey(name)
	}
	
	internal fun <T : Any> set(type: KClass<T>, binding: Binding<T>) {
		typeBindings[type] = binding
	}
	
	internal fun <T : Any> set(name: TypedName<T>, binding: Binding<T>) {
		namedBindings[name] = binding
	}
	
	internal fun <T : Any> create(type: KClass<T>): T {
		return create(type, emptyArray())
	}
	
	internal fun <T : Any> create(type: KClass<T>, additionalArgs: Array<Any>): T {
		val metadata = Metadata.getInject(type)
		val args = metadata.args.cutLast(additionalArgs.size).map {
			if (it is Array<*>)
				get(TypedName(it[0].unsafeCast<KClass<*>>(), it[1].unsafeCast<String>()))
			else
				get(it.unsafeCast<KClass<*>>())
		}
		return metadata.create(args.toTypedArray() + additionalArgs)
	}
}