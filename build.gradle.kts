plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")

        // isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("io.swagger.parser.v3:swagger-parser:2.0.31")
    implementation("com.github.davidmoten:guava-mini:0.1.4")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("net.sourceforge.plantuml:plantuml:1.2022.5-SNAPSHOT")
//    project(":plantuml")
    implementation("guru.nidi:graphviz-java:0.18.1")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.github.davidmoten:junit-extras:0.4")
}

group = "com.github.davidmoten"
version = parent?.version ?: "2022-07-4"
description = "OpenAPI To PlantUML"
java.sourceCompatibility = JavaVersion.VERSION_11

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
