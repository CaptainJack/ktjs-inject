import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin") version "1.2.41" apply false
	id("ru.capjack.degos.publish") version "1.5.0" apply false
	id("nebula.release") version "6.0.0"
}

subprojects {
	group = "ru.capjack.ktjs"
	repositories.maven("http://artifactory.capjack.ru/public")
	
	plugins.withId("java") {
		configure<JavaPluginConvention> {
			sourceCompatibility = JavaVersion.VERSION_1_8
		}
		tasks.withType<KotlinCompile> {
			kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
		}
	}
}
