import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

allprojects {
	group = "ru.capjack.ktjs"
	
	repositories {
		maven("http://artifactory.capjack.ru/public")
	}
}

plugins {
	id("kotlin2js") version "1.2.31"
	id("ru.capjack.degos-publish") version "1.4.1-dev.2+0a62163"
	id("nebula.release") version "6.0.0"
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.ktjs:ktjs-common:0.1.0-SNAPSHOT")
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		moduleKind = "amd"
	}
}
