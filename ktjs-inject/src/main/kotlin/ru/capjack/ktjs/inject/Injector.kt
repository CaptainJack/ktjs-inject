package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import kotlin.reflect.KClass

interface Injector {
	fun <T : Any> get(type: KClass<T>): T
	
	fun <T : Any> get(name: TypedName<T>): T
}

inline fun <reified T : Any> Injector.get(): T {
	return get(T::class)
}