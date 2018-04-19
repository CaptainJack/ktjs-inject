plugins {
	id("kotlin")
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("ru.capjack.degos-publish")
	id("nebula.release")
}

dependencies {
	compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
	(plugins) {
		"KtjsInject" {
			id = "ru.capjack.ktjs-inject-plugin"
			implementationClass = "ru.capjack.ktjs.inject.KtjsInjectPlugin"
		}
	}
}

tasks["processResources"].dependsOn(task("updateVersion") {
	doLast {
		file("src/main/resources/version.txt").writeText(version.toString())
	}
})

evaluationDependsOn(":ktjs-inject-plugin:compiler")
afterEvaluate {
	val projectCompiler = project(":ktjs-inject-plugin:compiler")
	tasks["processResources"].dependsOn(projectCompiler.tasks["build"])
	java.sourceSets["main"].resources.apply {
		srcDir((projectCompiler.tasks["jar"] as Jar).archivePath.parent)
	}
}