package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.InjectorImpl

internal abstract class ReplaceBinding<T : Any>(
	injector: InjectorImpl,
	provider: InjectorImpl.() -> T
) : ProviderBinding<T>(injector, provider) {
	
	override fun get(): T {
		val instance = super.get()
		replace(InstanceBinding(instance))
		return instance
	}
	
	abstract fun replace(binding: InstanceBinding<T>)
}