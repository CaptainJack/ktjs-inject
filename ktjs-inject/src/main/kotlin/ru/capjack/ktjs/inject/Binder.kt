package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import kotlin.reflect.KClass

interface Binder : ImplementationBinder {
	fun <T : Any> bindInstance(type: KClass<T>, instance: T)
	
	fun <T : Any> bindFactory(type: KClass<T>, factory: Injector.() -> T)
	
	fun <T : Any> bindProvider(type: KClass<T>, provider: Injector.() -> T)
	
	fun <T : Any> bind(name: TypedName<T>, implementation: KClass<T>)
	
	fun <T : Any> bindInstance(name: TypedName<T>, instance: T)
	
	fun <T : Any> bindFactory(name: TypedName<T>, factory: Injector.() -> T)
	
	fun <T : Any> bindProvider(name: TypedName<T>, provider: Injector.() -> T)
	
	fun <T : Any> bindProxy(type: KClass<T>): ProxyBinder
	
	fun <T : Any> bindProxy(name: TypedName<T>): ProxyBinder
}

interface ProxyBinder : ImplementationBinder {
	fun <T : Any> bind(method: String, implementation: KClass<T>)
}

interface ImplementationBinder {
	fun <T : Any, I : T> bind(type: KClass<T>, implementation: KClass<I>)
}

inline fun <reified T : Any> Binder.bindInstance(instance: T) {
	bindInstance(T::class, instance)
}

inline fun <reified T : Any> Binder.bindFactory(noinline factory: Injector.() -> T) {
	bindFactory(T::class, factory)
}

inline fun <reified T : Any> Binder.bindProvider(noinline provider: Injector.() -> T) {
	bindProvider(T::class, provider)
}

inline fun <reified T : Any> Binder.bindProxy(): ProxyBinder {
	return bindProxy(T::class)
}

inline fun <reified T : Any, reified I : T> Binder.bindReference() {
	bindFactory(T::class) { get(I::class) }
}

inline fun <reified T : Any> ImplementationBinder.bindSelf() {
	bind(T::class, T::class)
}

inline fun <reified T : Any, reified I : T> ImplementationBinder.bind() {
	bind(T::class, I::class)
}

inline fun <reified T : Any> ProxyBinder.bind(method: String) {
	bind(method, T::class)
}