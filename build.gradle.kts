plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "net.simax_dev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.2")


    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    runtimeOnly("org.apache.logging.log4j:log4j-api:2.17.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "net.simax_dev.example.ExampleApplication"
        attributes["Multi-Release"] = true
    }
}