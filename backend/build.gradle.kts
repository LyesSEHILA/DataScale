plugins {
    java
    id("org.springframework.boot") version "3.4.11"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "com.cyberscale"
version = "1.0.0"



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
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.github.docker-java:docker-java-core:3.7.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.7.0")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.cucumber:cucumber-java:7.15.0")
    testImplementation("io.cucumber:cucumber-spring:7.15.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.1")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.18.1")
    testImplementation("io.github.bonigarcia:webdrivermanager:5.7.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

// --- Configuration JaCoCo en Kotlin DSL ---

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    useJUnitPlatform()
    // Dit à Gradle de lancer le rapport APRES les tests
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    // S'assure que les tests ont tourné avant de générer le rapport
    dependsOn(tasks.test)

    reports {
        // Syntaxe Kotlin: on utilise .set() pour les propriétés booléennes
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

// ==================================================================
//  CONFIGURATION AUTO DES VARIABLES D'ENVIRONNEMENT (.env)
// ==================================================================

// Fonction pour charger le fichier .env
fun loadEnv(target: Any) {
    val envFile = file(".env")
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    
                    // Injection selon le type de tâche
                    if (target is JavaExec) {
                        target.environment(key, value)
                    } else if (target is Test) {
                        target.environment(key, value)
                    }
                }
            }
        }
        println("✅  [.env] Variables chargées pour : $target")
    } else {
        println("⚠️  [.env] Fichier introuvable ! (Pensez à copier .env.example)")
    }
}

// Appliquer au démarrage (bootRun)
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    loadEnv(this)
}

// Appliquer aux tests
tasks.withType<Test> {
    loadEnv(this)
}