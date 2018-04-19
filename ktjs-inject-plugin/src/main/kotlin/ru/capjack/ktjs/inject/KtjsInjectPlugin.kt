package ru.capjack.ktjs.inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.Kotlin2JsPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

open class KtjsInjectPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.plugins.apply(Kotlin2JsPluginWrapper::class.java)
		
		val version = javaClass.classLoader.getResource("version.txt").readText()
		val compiler = javaClass.classLoader.getResource("compiler.jar").path
		
		project.dependencies.add("implementation", "ru.capjack.ktjs:ktjs-inject:$version")
		
		val copyCompiler = project.task<Copy>("copyKtjsCompilerPlugin") {
			from(project.zipTree(compiler.substring(5).substringBefore('!')))
			into(project.buildDir.resolve("ktjs-inject"))
			include("compiler.jar")
		}
		
		project.tasks.withType<Kotlin2JsCompile> {
			dependsOn(copyCompiler)
			kotlinOptions.freeCompilerArgs += listOf("-Xplugin=${project.buildDir.resolve("ktjs-inject/compiler.jar")}")
		}
	}
}
