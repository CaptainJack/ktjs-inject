package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.Injector

internal open class ProviderBinding<out T : Any>(
	private val injector: Injector,
	private val provider: Injector.() -> T
) : Binding<T> {
	override fun get(): T = injector.provider()
}