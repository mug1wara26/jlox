plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "lox.Lox"
}

tasks.named<JavaExec>("run") {
    val loggingConfig = "${project.projectDir}/logging.properties"
    jvmArgs("-Djava.util.logging.config.file=$loggingConfig")

    standardInput = System.`in`

    enableAssertions = true
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "lox.Lox"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
