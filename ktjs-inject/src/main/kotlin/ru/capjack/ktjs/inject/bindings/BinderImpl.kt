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
		injector.set(type, TypeReplaceBinding(type, injector) { create(implementation) })
	}
	
	override fun <T : Any, I : T> bindMultiple(type: KClass<T>, implementation: KClass<I>) {
		injector.set(type, ProviderBinding(injector) { create(implementation) })
	}
	
	override fun <T : Any, I : T> bindInstance(type: KClass<T>, instance: I) {
		injector.set(type, InstanceBinding(instance))
	}
	
	override fun <T : Any, I : T> bindFactory(type: KClass<T>, factory: Injector.() -> I) {
		injector.set(type, TypeReplaceBinding(type, injector, factory))
	}
	
	override fun <T : Any, I : T> bindProvider(type: KClass<T>, factory: Injector.() -> I) {
		injector.set(type, ProviderBinding(injector, factory))
	}
	
	
	override fun <T : Any, I : T> bind(name: TypedName<T>, implementation: KClass<I>) {
		injector.set(name, NameReplaceBinding(name, injector) { create(implementation) })
	}
	
	override fun <T : Any, I : T> bindInstance(name: TypedName<T>, instance: I) {
		injector.set(name, InstanceBinding(instance))
	}
	
	override fun <T : Any, I : T> bindFactory(name: TypedName<T>, factory: Injector.() -> I) {
		injector.set(name, NameReplaceBinding(name, injector, factory))
	}
	
	override fun <T : Any, I : T> bindMultiple(name: TypedName<T>, implementation: KClass<I>) {
		injector.set(name, ProviderBinding(injector) { create(implementation) })
	}
	
	override fun <T : Any, I : T> bindProvider(name: TypedName<T>, factory: Injector.() -> I) {
		injector.set(name, ProviderBinding(injector, factory))
	}
	
	
	override fun <T : Any> bindProxy(type: KClass<T>): ProxyBinder {
		return ProxyBuilder(type).apply {
			injector.set(type, TypeReplaceBinding(type, injector, ::build))
		}
	}
	
	override fun <T : Any> bindProxy(name: TypedName<T>): ProxyBinder {
		return ProxyBuilder(name.type).apply {
			injector.set(name, NameReplaceBinding(name, injector, ::build))
		}
	}
}
