import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

buildscript {
	repositories {
		mavenCentral()
	}
}

plugins {
	java
	id("org.springframework.boot") version "2.7.3"
	id("io.spring.dependency-management") version "1.0.13.RELEASE"
	kotlin("jvm") version "1.7.10"
	kotlin("plugin.spring") version "1.7.10"
}

java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
	group = "fs.playground"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

}

subprojects {
	apply {
		plugin("java")
		plugin("org.springframework.boot")
		plugin("io.spring.dependency-management")
		plugin("org.jetbrains.kotlin.plugin.spring")
		plugin("kotlin")
		plugin("kotlin-spring")
	}

	dependencies {
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
		runtimeOnly("org.postgresql:postgresql")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "11"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}

tasks.named<Jar>("jar") {
	enabled = false
}

tasks.named<BootJar>("bootJar") {
	enabled = false
}