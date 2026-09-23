plugins {
    id("com.android.application") version "9.4.1" apply false
    id("app.cash.paparazzi") version "2.0.0-alpha05" apply false
    id("org.sonarqube") version "7.5.0.8588"
}

sonar {
    properties {
        property("sonar.projectKey", "KSmanis_QTTT")
        property("sonar.organization", "ksmanis")
        property("sonar.exclusions", "**/*.png")
    }
}

project(":app") {
    sonar {
        properties {
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                listOf(
                    layout.buildDirectory.file("reports/coverage/test/debug/report.xml").get().asFile,
                    layout.buildDirectory.file("reports/coverage/androidTest/debug/managedDevice/report.xml").get().asFile,
                ).joinToString(","),
            )
        }
    }
}

tasks.named("sonar") {
    if (!providers.gradleProperty("sonarCoverageReport").isPresent) {
        throw GradleException("sonar requires -PsonarCoverageReport")
    }
    dependsOn(":app:createDebugUnitTestCoverageReport", ":app:createManagedDeviceDebugAndroidTestCoverageReport", ":app:lintDebug")
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Deletes the root build directory."
    delete(layout.buildDirectory)
}
