package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import kotlin.reflect.KClass


interface Binder {
	fun <T : Any, I : T> bind(type: KClass<T>, implementation: KClass<I>)
	
	fun <T : Any, I : T> bindMultiple(type: KClass<T>, implementation: KClass<I>)
	
	fun <T : Any, I : T> bindInstance(type: KClass<T>, instance: I)
	
	fun <T : Any, I : T> bindFactory(type: KClass<T>, factory: Injector.() -> I)
	
	fun <T : Any, I : T> bindProvider(type: KClass<T>, factory: Injector.() -> I)
	
	
	fun <T : Any, I : T> bind(name: TypedName<T>, implementation: KClass<I>)
	
	fun <T : Any, I : T> bindInstance(name: TypedName<T>, instance: I)
	
	fun <T : Any, I : T> bindFactory(name: TypedName<T>, factory: Injector.() -> I)
	
	fun <T : Any, I : T> bindMultiple(name: TypedName<T>, implementation: KClass<I>)
	
	fun <T : Any, I : T> bindProvider(name: TypedName<T>, factory: Injector.() -> I)
	
	
	fun <T : Any> bindProxy(type: KClass<T>): ProxyBinder
	
	fun <T : Any> bindProxy(name: TypedName<T>): ProxyBinder
}


interface ProxyBinder {
	fun <T : Any, I : T> provides(type: KClass<T>, implementation: KClass<I>): ProxyBinder
	
	fun <T : Any> provides(method: String, implementation: KClass<T>): ProxyBinder
}

//

inline fun <reified T : Any, reified I : T> Binder.bind() =
	bind(T::class, I::class)

inline fun <reified T : Any> Binder.bindInstance(instance: T) =
	bindInstance(T::class, instance)

inline fun <reified T : Any> Binder.bindFactory(noinline factory: Injector.() -> T) =
	bindFactory(T::class, factory)

inline fun <reified T : Any, reified I : T> Binder.bindMultiple() =
	bindMultiple(T::class, I::class)

inline fun <reified T : Any> Binder.bindProvider(noinline factory: Injector.() -> T) =
	bindProvider(T::class, factory)

inline fun <reified T : Any> Binder.bindProxy() =
	bindProxy(T::class)

inline fun <reified T : Any> Binder.bindSelf() =
	bind(T::class, T::class)

inline fun <reified T : Any, reified I : T> Binder.bindReference() =
	bindProvider(T::class) { get(I::class) }

inline fun <T : Any, reified I : T> Binder.bindReference(name: TypedName<T>) =
	bindProvider(name) { get(I::class) }


inline fun <reified T : Any, reified I : T> ProxyBinder.provides() =
	provides(T::class, I::class)

inline fun <reified T : Any> ProxyBinder.provides(method: String) =
	provides(method, T::class)

inline fun <reified T : Any> ProxyBinder.providesSelf() =
	provides(T::class, T::class)