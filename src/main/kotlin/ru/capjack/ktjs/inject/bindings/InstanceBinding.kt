package ru.capjack.ktjs.inject.bindings

internal class InstanceBinding<out T : Any>(private val instance: T) : Binding<T> {
	override fun get(): T = instance
}