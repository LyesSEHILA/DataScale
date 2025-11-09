plugins {
	java
	id("org.springframework.boot") version "3.4.11"
	id("io.spring.dependency-management") version "1.1.7"
	id 'jacoco'
}

group = "com.cyberscale"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12" // Version récente de JaCoCo
}

tasks.named('test') {
    finalizedBy jacocoTestReport // Dit à Gradle de lancer le rapport APRÈS les tests
}

jacocoTestReport {
    dependsOn tasks.named('test')
    reports {
        xml.required = true // Crucial : Force la création du rapport XML pour Sonar
        csv.required = false
        html.required = true // (Bien pour voir le rapport en local)
    }
}