package ru.capjack.ktjs.inject

import ru.capjack.ktjs.inject.bindings.BinderImpl

class InjectorBuilder {
	private val configurations: MutableList<InjectConfiguration> = mutableListOf()
	
	fun configure(configuration: InjectConfiguration): InjectorBuilder {
		configurations.add(configuration)
		return this
	}
	
	fun configure(configuration: Binder.() -> Unit): InjectorBuilder {
		return configure(object : InjectConfiguration {
			override fun configure(binder: Binder) = configuration.invoke(binder)
		})
	}
	
	fun build(strong: Boolean = true): Injector {
		val injector = InjectorImpl()
		val binder = BinderImpl(injector, strong)
		configurations.forEach { it.configure(binder) }
		return injector
	}
}

