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

include(":core:ui")

include(":sync")

include(":feature:intro")

include(":feature:location")

include(":feature:day")
