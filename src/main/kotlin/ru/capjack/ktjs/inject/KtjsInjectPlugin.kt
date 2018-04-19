package ru.capjack.ktjs.inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

open class KtjsInjectPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		val compiler = javaClass.classLoader.getResource(":compiler.jar").path
		val runtime = javaClass.classLoader.getResource("runtime.jar").path
		
		project.dependencies.add("implementation", project.files(runtime))
		
		project.tasks.withType<Kotlin2JsCompile> {
			kotlinOptions.freeCompilerArgs += listOf("-Xplugin=$compiler")
		}
	}
}
