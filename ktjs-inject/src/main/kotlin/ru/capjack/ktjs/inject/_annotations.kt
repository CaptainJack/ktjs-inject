package ru.capjack.ktjs.inject

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class Inject

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Bind(
	val implementation: KClass<*>,
	val multiple: Boolean = false
)

@Target(AnnotationTarget.CLASS)
annotation class BindSelf(
	val multiple: Boolean = false
)

@Target(AnnotationTarget.CLASS)
annotation class Proxy

@Target(AnnotationTarget.CLASS)
annotation class BindProxy


@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Name(
	val name: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class Provides(
	val implementation: KClass<*>
)
