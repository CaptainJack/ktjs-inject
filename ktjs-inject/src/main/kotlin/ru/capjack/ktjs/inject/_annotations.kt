package ru.capjack.ktjs.inject

@Target(AnnotationTarget.CLASS)
annotation class Inject

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class InjectName(val name: String)

@Target(AnnotationTarget.CLASS)
annotation class InjectProxy
