package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import kotlin.reflect.KClass

interface Injector {
	operator fun <T : Any> get(type: KClass<T>): T
	
	operator fun <T : Any> KClass<T>.unaryPlus(): T
	
	operator fun <T : Any> get(name: TypedName<T>): T
	
	operator fun <T : Any> TypedName<T>.unaryPlus(): T
}