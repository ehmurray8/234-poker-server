plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
    checkstyle
}

group = "dev.emurray"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val checkstyleConfig: Configuration by configurations.creating
dependencies {
    checkstyleConfig("com.puppycrawl.tools:checkstyle:9.3") {
        isTransitive = false
    }
}

configure<CheckstyleExtension> {
    configFile = File("config/checkstyle/checkstyle.xml")
    configDirectory.set(File(rootDir, "config/checkstyle"))
    configProperties = mapOf("suppressionFile" to configDirectory.get().file("checkstyle-suppressions.xml").asFile)
    toolVersion = "9.3"
    sourceSets = listOf(project.sourceSets["main"])
}

sourceSets.getByName("main") {
    java.srcDir("src/main/java")
}
sourceSets.getByName("test") {
    java.srcDir("src/test/java")
}

repositories {
    mavenCentral()
}

repositories {
	mavenCentral()
}

val immutablesVersion = "2.10.1"
val recordBuilderVersion = "41"
val guavaVersion = "33.1.0-jre"
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.google.guava:guava:$guavaVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor("io.soabase.record-builder:record-builder-processor:$recordBuilderVersion")
    compileOnly("io.soabase.record-builder:record-builder-core:$recordBuilderVersion")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
