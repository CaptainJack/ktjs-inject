package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.inject.InjectorImpl

internal abstract class InstanceDependentBinding<T : Any>(
	protected val injector: InjectorImpl
) : Binding<T>

