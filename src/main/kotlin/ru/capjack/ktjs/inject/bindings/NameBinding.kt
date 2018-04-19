package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.inject.Injector
import ru.capjack.ktjs.inject.InjectorImpl

internal class NameBinding<out T : Any>(
	private val injector: InjectorImpl,
	private val name: TypedName<T>,
	create: Injector.() -> T
) : ProviderBinding<T>(injector, create) {
	
	override fun get(): T {
		val instance = super.get()
		injector.setBinding(name, InstanceBinding(instance))
		return instance
	}
}