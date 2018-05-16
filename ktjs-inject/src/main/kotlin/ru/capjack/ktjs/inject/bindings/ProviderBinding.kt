package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.InjectorImpl

internal open class ProviderBinding<T : Any>(
	injector: InjectorImpl,
	private val provider: InjectorImpl.() -> T
) : InstanceDependentBinding<T>(injector) {
	override fun get(): T = provider.invoke(injector)
}