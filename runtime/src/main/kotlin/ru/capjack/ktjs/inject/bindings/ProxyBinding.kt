package ru.capjack.ktjs.inject.bindings

import ru.capjack.ktjs.common.cutLast
import ru.capjack.ktjs.common.js.jso
import ru.capjack.ktjs.inject.InjectMetadata
import ru.capjack.ktjs.inject.InjectorImpl
import ru.capjack.ktjs.inject.Metadata
import kotlin.reflect.KClass

internal class ProxyBinding<out T : Any>(
    private val injector: InjectorImpl,
    type: KClass<T>,
    map: Map<KClass<*>, KClass<*>>
) : Binding<T> {

    private val proxy: dynamic = jso()

    init {
        val methods: Array<Array<*>> = Metadata.getMetadataInjectProxy(type)

        for (metadata in methods) {
            val impl = map[metadata[1]]
            if (impl == null) {
                addMethod(metadata, metadata[1].unsafeCast<KClass<*>>())
            } else {
                addMethod(metadata, impl)
            }
        }
    }

    override fun get(): T = proxy.unsafeCast<T>()

    private fun addMethod(m: Array<*>, impl: KClass<*>) {
        val methodName = m[0].unsafeCast<String>()
        val methodArgs = m[2].unsafeCast<Int>()

        val inject: InjectMetadata<*> = Metadata.getMetadataInject(impl)

        if (methodArgs == 0) {
            val types = inject.types

            proxy[methodName] = {
                inject.create(Metadata.getInstances(injector, types))
            }
        } else {
            val types = inject.types.cutLast(methodArgs)

            proxy[methodName] = {
                val values = Metadata.getInstances(injector, types)
                val args = js("Array.prototype.slice.call(arguments)").unsafeCast<Array<Any>>()
                inject.create(values + args)
            }
        }
    }

}

