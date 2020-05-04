plugins {
    kotlin("jvm")
}

val assetsDir = file("../android/assets")
val mainClassName = "com.libktx.desktop.DesktopLauncher"

dependencies {
    val gdxVersion: String by project
    val ktxVersion: String by project
//    val coroutinesVersion : String by project

    implementation(project(":core"))

    implementation(kotlin("stdlib"))
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("io.github.libktx:ktx-app:$ktxVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

// Use this task to run the game if IntelliJ run application configuration doesn't work.
tasks.register<JavaExec>("run") {
    main = mainClassName
    classpath = sourceSets.main.get().runtimeClasspath
    standardInput = System.`in`
    workingDir = assetsDir
    isIgnoreExitValue = true

    if ("mac" in System.getProperty("os.name").toLowerCase()) {
        jvmArgs("-XstartOnFirstThread")
    }
}

// Use this task to create a fat jar.
tasks.register<Jar>("dist") {
    from(files(sourceSets.main.get().output.classesDirs))
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(assetsDir)

    manifest {
        attributes["Main-Class"] = mainClassName
    }
}