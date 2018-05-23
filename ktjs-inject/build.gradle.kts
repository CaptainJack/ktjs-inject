import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
	id("kotlin2js")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.ktjs:ktjs-common:0.2.0-SNAPSHOT")
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		moduleKind = "amd"
	}
}