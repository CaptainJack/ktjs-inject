package ru.capjack.ktjs.inject.bindings

internal interface Binding<out T : Any> {
	fun get(): T
}

