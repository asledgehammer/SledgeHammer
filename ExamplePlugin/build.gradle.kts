plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
}

group = "com.asledgehammer"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(project(":CraftHammer_Util"))
    implementation(project(":CraftHammer_API"))
    implementation(project(":CraftHammer"))
    implementation(project(":SledgeHammer"))
}
