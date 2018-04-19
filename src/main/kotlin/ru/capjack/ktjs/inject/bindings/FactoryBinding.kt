package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.Injector
import ru.capjack.ktjs.inject.InjectorImpl
import kotlin.reflect.KClass

internal open class FactoryBinding<out T : Any>(
	private val injector: InjectorImpl,
	private val type: KClass<T>,
	create: Injector.() -> T
) : ProviderBinding<T>(injector, create) {
	
	override fun get(): T {
		val instance = super.get()
		injector.setBinding(type, InstanceBinding(instance))
		return instance
	}
}