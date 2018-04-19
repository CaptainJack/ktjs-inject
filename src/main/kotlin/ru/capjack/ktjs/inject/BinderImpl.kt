package ru.capjack.ktjs.inject

import ru.capjack.ktjs.common.TypedName
import ru.capjack.ktjs.inject.bindings.Binding
import ru.capjack.ktjs.inject.bindings.FactoryBinding
import ru.capjack.ktjs.inject.bindings.InstanceBinding
import ru.capjack.ktjs.inject.bindings.NameBinding
import ru.capjack.ktjs.inject.bindings.ProviderBinding
import ru.capjack.ktjs.inject.bindings.ProxyBinding
import kotlin.reflect.KClass

internal class BinderImpl(
	private val injector: InjectorImpl,
	private val strong: Boolean
) : Binder {
	
	override fun <T : Any, I : T> bind(type: KClass<T>, impl: KClass<I>) {
		val metadata: InjectMetadata<I> = Metadata.getMetadataInject(impl)
		bindFactory(type) {
			metadata.create(Metadata.getInstances(injector, metadata.types))
		}
	}
	
	override fun <T : Any> bindFactory(type: KClass<T>, factory: Injector.() -> T) {
		addBinding(type, FactoryBinding(injector, type, factory))
	}
	
	override fun <T : Any> bindInstance(type: KClass<T>, instance: T) {
		addBinding(type, InstanceBinding(instance))
	}
	
	override fun <T : Any> bindProvider(type: KClass<T>, provider: Injector.() -> T) {
		addBinding(type, ProviderBinding(injector, provider))
	}
	
	override fun <T : Any> bindFactory(name: TypedName<T>, factory: Injector.() -> T) {
		addBinding(name, NameBinding(injector, name, factory))
	}
	
	override fun <T : Any> bindInstance(name: TypedName<T>, instance: T) {
		addBinding(name, InstanceBinding(instance))
	}
	
	override fun <T : Any> bindProvider(name: TypedName<T>, provider: Injector.() -> T) {
		addBinding(name, ProviderBinding(injector, provider))
	}

	override fun <T : Any> bindProxy(type: KClass<T>) {
		addBinding(type, ProxyBinding(injector, type, emptyMap()))
	}

	override fun <T : Any> bindProxy(type: KClass<T>, binder: ProxyBinder.() -> Unit) {
		val map: MutableMap<KClass<*>, KClass<*>> = mutableMapOf()
		
		binder.invoke(
			object : ProxyBinder {
				override fun <T : Any, I : T> bind(type: KClass<T>, impl: KClass<I>) {
					map[type] = impl
				}
			}
		)
		
		addBinding(type, ProxyBinding(injector, type, map))
	}
	
	override fun <T : Any, I : Any> bindProxySimple(type: KClass<T>, impl: KClass<I>) {
		val methods: Array<Array<*>> = Metadata.getMetadataInjectProxy(type)
		if (methods.isEmpty()) {
			throw IllegalStateException()
		}
		
		val method = methods[0]
		
		bindProxy(type) {
			bind(method[1].unsafeCast<KClass<I>>(), impl)
		}
	}
	
	private fun <T : Any> addBinding(type: KClass<T>, binding: Binding<T>) {
		if (strong && injector.containsBinding(type)) {
			throw IllegalStateException("Binding for class \"${type.simpleName}\" was already defined")
		}
		injector.setBinding(type, binding)
	}
	
	private fun <T : Any> addBinding(name: TypedName<T>, binding: Binding<T>) {
		if (strong && injector.containsBinding(name)) {
			throw IllegalStateException("Binding for name \"$name\" was already defined")
		}
		injector.setBinding(name, binding)
	}
}