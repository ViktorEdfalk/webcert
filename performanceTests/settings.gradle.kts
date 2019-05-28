import se.inera.webcert.performance.Dependencies.gatlingPluginVersion

pluginManagement {
  repositories {
    maven("https://build-inera.nordicmedtest.se/nexus/repository/releases/")
    gradlePluginPortal()
    jcenter()
  }

  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("com.github.lkishalmi.gatling")) {
        useVersion(gatlingPluginVersion)
      }
    }
  }
}

rootProject.name = "webcert-performance-test"
