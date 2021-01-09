import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("kapt")
	kotlin("plugin.spring")
	id("com.google.cloud.tools.jib")
}

group = "com.ngenenius.api"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val springBootVersion: String by project
val springCloudVersion: String by project
val kotlinVersion: String by project
val kotlinLogging: String by project

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.github.microutils:kotlin-logging-jvm")
	runtimeOnly("org.springframework.boot:spring-boot-devtools")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	// literally only here to make IntelliJ happy - magic happens from the 'kapt' one. May not work with @ConstructorBinding?
	compileOnly("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
			bomProperty("kotlin.version", kotlinVersion)
		}
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
		dependencies {
			dependency("io.github.microutils:kotlin-logging-jvm:$kotlinLogging")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

jib.to.image = "ngeniusgaming/ngen-api:${project.findProperty("docker.tag") ?: "latest"}"
