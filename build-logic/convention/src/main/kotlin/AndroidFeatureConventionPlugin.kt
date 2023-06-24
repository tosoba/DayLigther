import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin

class AndroidFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply {
        apply("daylighter.android.library")
        apply("daylighter.android.hilt")
      }

      extensions.configure<LibraryExtension> {
        defaultConfig {
          testInstrumentationRunner = "com.trm.daylighter.core.testing.DayLighterTestRunner"
        }
      }

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      dependencies {
        add("implementation", project(":core:data"))
        add("implementation", project(":core:common"))
        add("implementation", project(":core:domain"))
        add("implementation", project(":core:ui"))

        add("testImplementation", kotlin("test"))
        add("androidTestImplementation", kotlin("test"))

        add("implementation", libs.findLibrary("coil.kt").get())
        add("implementation", libs.findLibrary("coil.kt.compose").get())

        add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())

        add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
      }
    }
  }
}
