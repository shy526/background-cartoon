plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    java
}

group = "com.github.shy526"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

}
dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.3.1")
}
tasks {
    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}


tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}


