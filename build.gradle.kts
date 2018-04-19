import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
	group = "ru.capjack.ktjs"
	
	repositories {
		maven("http://artifactory.capjack.ru/public")
	}
}

plugins {
	id("kotlin") version "1.2.31"
	
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("ru.capjack.degos-publish") version "1.4.0"
	id("nebula.release") version "6.0.0"
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	compileClasspath(kotlin("gradle-plugin"))
}

gradlePlugin {
	(plugins) {
		"KtjsInject" {
			id = "ru.capjack.ktjs-inject"
			implementationClass = "ru.capjack.ktjs.inject.KtjsInjectPlugin"
		}	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

val projectCompiler = project(":compiler")
val projectRuntime = project(":runtime")

evaluationDependsOn(":${projectCompiler.name}")
evaluationDependsOn(":${projectRuntime.name}")
afterEvaluate {
	tasks["processResources"].dependsOn(projectCompiler.tasks["jar"], projectRuntime.tasks["jar"])
	java.sourceSets["main"].resources.apply {
		srcDir((projectCompiler.tasks["jar"] as Jar).archivePath.parent)
		srcDir((projectRuntime.tasks["jar"] as Jar).archivePath.parent)
	}
}
