plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

application {
    mainClass.set("com.example.kmp.tools.CreateServiceKt")
}

tasks.jar {
    archiveFileName.set("tools.jar")
    manifest {
        attributes["Main-Class"] = "com.example.kmp.tools.CreateServiceKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
