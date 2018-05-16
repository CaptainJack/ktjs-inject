package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.InjectorImpl
import kotlin.reflect.KClass

internal class TypeReplaceBinding<T : Any>(
	private val type: KClass<T>,
	injector: InjectorImpl,
	provider: InjectorImpl.() -> T
) : ReplaceBinding<T>(injector, provider) {
	override fun replace(binding: InstanceBinding<T>) {
		injector.set(type, binding)
	}
}