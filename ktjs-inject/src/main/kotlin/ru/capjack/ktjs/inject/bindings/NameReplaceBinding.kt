package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.InjectorImpl
import ru.capjack.ktjs.inject.TypedName

internal class NameReplaceBinding<T : Any>(
	private val name: TypedName<T>,
	injector: InjectorImpl,
	provider: InjectorImpl.() -> T
) : ReplaceBinding<T>(injector, provider) {
	override fun replace(binding: InstanceBinding<T>) {
		injector.set(name, binding)
	}
}

