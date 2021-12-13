import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("kapt")
	kotlin("plugin.spring")
}

group = "com.ngenenius.api"
java.sourceCompatibility = JavaVersion.VERSION_11

val springBootVersion: String by project
val springCloudVersion: String by project
val kotlinVersion: String by project
val kotlinLogging: String by project
val caffeineVersion: String by project
val springdocVersion: String by project


val mockitoKotlinVersion: String by project
val assertJVersion: String by project


repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging-jvm")
	implementation("com.github.ben-manes.caffeine:caffeine")
	implementation("org.springdoc:springdoc-openapi-ui:1.5.2")
	runtimeOnly("org.springframework.boot:spring-boot-devtools")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	// literally only here to make IntelliJ happy - magic happens from the 'kapt' one. May not work with @ConstructorBinding?
	compileOnly("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.mockito:mockito-inline")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
	testImplementation("com.squareup.okhttp3:mockwebserver")
	testImplementation("org.assertj:assertj-core")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
			bomProperty("kotlin.version", kotlinVersion)
		}
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
		dependencies {
			dependency("io.github.microutils:kotlin-logging-jvm:$kotlinLogging")
			dependency("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
			dependency("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
			dependency("org.assertj:assertj-core:$assertJVersion")
			dependency("org.springdoc:springdoc-openapi-ui:$springdocVersion")
			dependency("org.apache.logging.log4j:log4j-api:[2.15.0,)")
			dependency("org.apache.logging.log4j:log4j-to-slf4j:[2.15.0,)")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
	imageName = "ngeniusgaming/ngen-api:${project.findProperty("docker.tag") ?: "latest"}"
	isPublish = project.hasProperty("docker.publish")
	docker {
		publishRegistry {
			username = project.findProperty("docker.username")?.toString() ?: "user"
			password = project.findProperty("docker.password")?.toString() ?: "secret"
			url = "https://registry.hub.docker.com/"
			email = "ngeniusgaming@noreply.com"
		}
	}
}
