import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    id("org.flywaydb.flyway") version "10.12.0"
    id("nu.studer.jooq") version "9.0"
}

group = "com.gmail.mk2543"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion("3.18.14")
        }
    }
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

kotlin {
    sourceSets {
        main {
            resources.srcDir("src/generated/jooq")
        }
    }
}

val integrationTest: SourceSet get() = sourceSets["integrationTest"]

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath + integrationTest.output

    shouldRunAfter(tasks.test)
}

tasks.named("check") {
    dependsOn("integrationTest")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jooq:jooq-kotlin:3.18.14")
    implementation("org.jooq:jooq:3.18.14")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("io.mockk:mockk:1.13.10")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:postgresql")

    runtimeOnly("com.h2database:h2:2.1.214")
    jooqGenerator("com.h2database:h2:2.1.214")
    jooqGenerator("org.jooq:jooq-codegen:3.18.14")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val jooqDb = mapOf(
    "url" to "jdbc:h2:${project.buildDir}/generated/flyway/h2",
    "schema" to "PUBLIC",
    "user" to "sa",
    "password" to ""
)

flyway {
    url = jooqDb["url"]
    user = jooqDb["user"]
    password = jooqDb["password"]
    schemas = arrayOf(jooqDb["schema"])
}

val migrationDirs = listOf(
    "$projectDir/src/main/resources/db/migration",
)
tasks.flywayMigrate {
    migrationDirs.forEach { inputs.dir(it) }
    outputs.dir("${project.buildDir}/generated/flyway")
    doFirst { delete(outputs.files) }
}

jooq {
    version = "3.18.14"
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation = true
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN

                jdbc.apply {
                    username = jooqDb["user"]
                    password = jooqDb["password"]
                    driver = "org.h2.Driver"
                    url = jooqDb["url"]
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    target.apply {
                        packageName = "com.gmail.mk2543.inventory.jooq"
                        directory = "src/generated/jooq"
                    }

                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
					inputSchema = jooqDb["schema"]
                    }
                }
            }
        }
    }
}

tasks.named("generateJooq") {
    dependsOn("flywayMigrate")
}

tasks.named("processResources") {
    dependsOn("generateJooq")
}
