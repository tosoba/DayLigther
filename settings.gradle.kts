import java.net.URI

pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
  }
}

rootProject.name = "DayLighter"

include(":app")

include(":core:common")

include(":core:network")

include(":core:data")

include(":core:database")

include(":core:datastore")

include(":core:domain")

include(":core:testing")

include(":core:ui")

include(":feature:about")

include(":feature:day")

include(":feature:intro")

include(":feature:location")

include(":feature:locations")

include(":feature:settings")

include(":feature:widget-location")

include(":work")

include(":ui-test-hilt-manifest")

include(":widget")

