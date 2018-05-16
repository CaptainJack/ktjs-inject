package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.inject.Binder
import ru.capjack.ktjs.inject.Injector
import ru.capjack.ktjs.inject.InjectorImpl
import ru.capjack.ktjs.inject.ProxyBinder
import kotlin.reflect.KClass

internal class BinderImpl(
	private val injector: InjectorImpl,
	private val strong: Boolean
) : Binder {
	override fun <T : Any, I : T> bind(type: KClass<T>, implementation: KClass<I>) {
		injector.set(type, TypeReplaceBinding(type, injector, { create(implementation) }))
	}
	
	override fun <T : Any> bindInstance(type: KClass<T>, instance: T) {
		injector.set(type, InstanceBinding(instance))
	}
	
	override fun <T : Any> bindFactory(type: KClass<T>, factory: Injector.() -> T) {
		injector.set(type, TypeReplaceBinding(type, injector, factory))
	}
	
	override fun <T : Any> bindProvider(type: KClass<T>, provider: Injector.() -> T) {
		injector.set(type, ProviderBinding(injector, provider))
	}
	
	override fun <T : Any> bind(name: TypedName<T>, implementation: KClass<T>) {
		injector.set(name, NameReplaceBinding(name, injector, { create(implementation) }))
	}
	
	override fun <T : Any> bindInstance(name: TypedName<T>, instance: T) {
		injector.set(name, InstanceBinding(instance))
	}
	
	override fun <T : Any> bindFactory(name: TypedName<T>, factory: Injector.() -> T) {
		injector.set(name, NameReplaceBinding(name, injector, factory))
	}
	
	override fun <T : Any> bindProvider(name: TypedName<T>, provider: Injector.() -> T) {
		injector.set(name, ProviderBinding(injector, provider))
	}
	
	override fun <T : Any> bindProxy(type: KClass<T>): ProxyBinder {
		val builder = ProxyBuilder(type)
		injector.set(type, TypeReplaceBinding(type, injector, builder::build))
		return builder
	}
	
	override fun <T : Any> bindProxy(name: TypedName<T>): ProxyBinder {
		val builder = ProxyBuilder(name.type)
		injector.set(name, NameReplaceBinding(name, injector, builder::build))
		return builder
	}
}
