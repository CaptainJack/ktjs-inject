package ru.capjack.ktjs.inject.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

open class KtjsInjectPlugin : Plugin<Project> {
	
	private val version: String = loadVersionFromResource()
	
	override fun apply(project: Project) {
		configureDefaultVersionsResolutionStrategy(project)
		
		project.configurations.create("ktjsInjectCompiler") { it.isTransitive = false }
		project.dependencies.add("ktjsInjectCompiler", "ru.capjack.ktjs:ktjs-inject-compiler")
		
		project.afterEvaluate {
			project.tasks.withType(Kotlin2JsCompile::class.java) {
				it.kotlinOptions.freeCompilerArgs += project.configurations.getByName("ktjsInjectCompiler").map {
					"-Xplugin=" + it.absolutePath
				}
			}
		}
	}
	
	private fun loadVersionFromResource(): String {
		return javaClass.classLoader.getResource("ktjs-inject-version").readText()
	}
	
	private fun configureDefaultVersionsResolutionStrategy(project: Project) {
		project.configurations.all { configuration ->
			configuration.resolutionStrategy.eachDependency { details ->
				val requested = details.requested
				if (requested.group == "ru.capjack.ktjs" && requested.name.startsWith("ktjs-inject") && requested.version.isEmpty()) {
					details.useVersion(version)
				}
			}
		}
	}
}
