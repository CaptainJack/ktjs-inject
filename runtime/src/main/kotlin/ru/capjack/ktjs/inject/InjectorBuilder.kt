package ru.capjack.ktjs.inject

class InjectorBuilder {
	private val configurations: MutableList<InjectConfiguration> = mutableListOf()
	
	fun configure(configuration: InjectConfiguration): InjectorBuilder {
		configurations.add(configuration)
		return this
	}
	
	fun configure(configuration: Binder.() -> Unit): InjectorBuilder {
		return configure(object : InjectConfiguration {
			override fun configure(binder: Binder) {
				binder.configuration()
			}
		})
	}
	
	fun build(strongBinding: Boolean = true): Injector {
		val injector = InjectorImpl()
		val binder = BinderImpl(injector, strongBinding)
		
		for (configuration in configurations) {
			configuration.configure(binder)
		}
		return injector
	}
}

