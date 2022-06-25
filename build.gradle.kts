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
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.2")

    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    runtimeOnly("org.apache.logging.log4j:log4j-api:2.17.2")

    implementation("org.reflections:reflections:0.10.2")
    runtimeOnly("org.reflections:reflections:0.10.2")

    implementation("org.jsoup:jsoup:1.14.3")
    runtimeOnly("org.jsoup:jsoup:1.14.3")
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