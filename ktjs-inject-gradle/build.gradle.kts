plugins {
	id("kotlin")
	id("ru.capjack.degos.publish")
	`java-gradle-plugin`
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
	(plugins) {
		"KtjsInject" {
			id = "ru.capjack.ktjs.inject"
			implementationClass = "ru.capjack.ktjs.inject.gradle.KtjsInjectPlugin"
		}
	}
}


tasks.withType<ProcessResources> {
	expand(project.properties)
}


//processResources {
//	expand(project.properties)
//}