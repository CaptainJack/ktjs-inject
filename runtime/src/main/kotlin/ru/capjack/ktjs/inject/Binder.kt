package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import kotlin.reflect.KClass

interface Binder {
    fun <T : Any, I : T> bind(type: KClass<T>, impl: KClass<I>)

    fun <T : Any> bindInstance(type: KClass<T>, instance: T)

    fun <T : Any> bindFactory(type: KClass<T>, factory: Injector.() -> T)

    fun <T : Any> bindProvider(type: KClass<T>, provider: Injector.() -> T)

    fun <T : Any> bindFactory(name: TypedName<T>, factory: Injector.() -> T)

    fun <T : Any> bindInstance(name: TypedName<T>, instance: T)

    fun <T : Any> bindProvider(name: TypedName<T>, provider: Injector.() -> T)

    fun <T : Any> bindProxy(type: KClass<T>)

    fun <T : Any> bindProxy(type: KClass<T>, binder: ProxyBinder.() -> Unit)

    fun <T : Any, I : Any> bindProxySimple(type: KClass<T>, impl: KClass<I>)

    fun <T : Any> bind(type: KClass<T>) {
        bind(type, type)
    }

    fun <T : Any, I : T> bindReference(type: KClass<T>, impl: KClass<I>) {
        bindFactory(type) { get(impl) }
    }
}

interface ProxyBinder {
    fun <T : Any, I : T> bind(type: KClass<T>, impl: KClass<I>)

    fun <T : Any, I : T> bind(type: KClass<T>) {
        bind(type, type)
    }
}