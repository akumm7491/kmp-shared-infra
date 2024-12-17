plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.example.kmp.tooling.ScaffoldToolKt")
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
}
