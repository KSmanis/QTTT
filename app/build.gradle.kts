import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

plugins {
    id("com.android.application")
    id("app.cash.paparazzi")
}

val versionNameValue = rootProject.file("version.txt").readText().trim()
val versionParts = versionNameValue.split('.')
val versionCodeValue = versionParts[0].toInt() * 1_000_000 + versionParts[1].toInt() * 1_000 + versionParts[2].toInt()
val sonarCoverageReport = providers.gradleProperty("sonarCoverageReport").isPresent

android {
    namespace = "com.gmail.smanis.konstantinos.qttt"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gmail.smanis.konstantinos.qttt"
        minSdk = 23
        targetSdk = 36
        versionCode = versionCodeValue
        versionName = versionNameValue
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            enableAndroidTestCoverage = sonarCoverageReport
            enableUnitTestCoverage = sonarCoverageReport
        }
        getByName("release") {
            optimization {
                enable = true
            }
        }
    }
    testOptions {
        animationsDisabled = true
        managedDevices {
            localDevices {
                create("pixel2Api36") {
                    device = "Pixel 2"
                    sdkVersion = 36
                    systemImageSource = "aosp-atd"
                    testedAbi = "x86_64"
                }
                if (!sonarCoverageReport) {
                    create("pixel2Api27") {
                        device = "Pixel 2"
                        sdkVersion = 27
                        systemImageSource = "aosp"
                        testedAbi = "x86"
                    }
                    create("pixel2Api37") {
                        device = "Pixel 2"
                        sdkVersion = 37
                        systemImageSource = "google"
                        testedAbi = "x86_64"
                    }
                }
            }
        }
    }
}

val javaVersion = Regex("(?m)^java = \"(\\d+)")
    .find(rootProject.file("mise.toml").readText())!!
    .groupValues[1].toInt()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

val openingBookClasses = layout.buildDirectory.dir("opening-book/classes")
val openingBookSources = files(
    "src/main/java/com/gmail/smanis/konstantinos/qttt/GameResult.java",
    "src/main/java/com/gmail/smanis/konstantinos/qttt/Move.java",
    "src/main/java/com/gmail/smanis/konstantinos/qttt/State.java",
    "src/main/java/com/gmail/smanis/konstantinos/qttt/Utility.java",
    rootProject.file("tools/OpeningBookGenerator.java"),
)

val compileOpeningBookGenerator = tasks.register<JavaCompile>("compileOpeningBookGenerator") {
    group = "opening book"
    description = "Compiles the opening book generator."
    source(openingBookSources)
    destinationDirectory.set(openingBookClasses)
    classpath = files()
    options.release.set(javaVersion)
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("generateOpeningBook") {
    group = "opening book"
    description = "Generates the opening book for the requested turn."
    val openingBookTurn = providers.gradleProperty("turn").orElse("")

    dependsOn(compileOpeningBookGenerator)
    classpath = files(openingBookClasses)
    mainClass.set("com.gmail.smanis.konstantinos.qttt.OpeningBookGenerator")
    workingDir = rootProject.projectDir
    doFirst {
        args(openingBookTurn.get())
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidxTestJunit)
    androidTestImplementation(libs.espressoCore)
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.withType<Test>().configureEach {
    if (sonarCoverageReport && name == "testDebugUnitTest") {
        // JaCoCo and Paparazzi cannot instrument the same test process.
        // https://github.com/cashapp/paparazzi/issues/1402
        // https://github.com/cashapp/paparazzi/issues/2320
        exclude("**/*SnapshotTest.class")
    }
}
