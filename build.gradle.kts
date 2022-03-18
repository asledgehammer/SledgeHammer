plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

apply(plugin = "java")
apply(plugin = "maven-publish")

val VERSION = "${findProperty("PZ_VERSION")!!}__${findProperty("SLEDGEHAMMER_VERSION")!!}"

group = "com.asledgehammer"
version = VERSION

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(project(":CraftHammer"))
    implementation(project(":CraftNail"))
    implementation(project(":CraftHammer_API"))
    implementation(project(":CraftHammer_Util"))
    // PZ Libraries
    compileOnly(fileTree("../lib") {
        include("*.jar")
    })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

tasks.register<Copy>("binClass") {
    doFirst {
        val dir = File("${projectDir}/../bin/class")
        if (!dir.exists()) dir.mkdirs()
    }
    from(
        layout.buildDirectory.dir("./classes/java/main"),
        layout.buildDirectory.dir("./classes/kotlin/main"),
        layout.buildDirectory.dir("./resources/")
    )
    into(layout.projectDirectory.dir("../bin/class"))
    exclude("**/META-INF")
}

tasks.register<Copy>("binJar") {
    doFirst{
        val dir = File("${projectDir}/../bin/jar")
        if(!dir.exists()) dir.mkdirs()
    }
    from(layout.buildDirectory.dir("./libs"))
    into(layout.projectDirectory.dir("../bin/jar"))
}
