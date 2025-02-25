import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferExtension

plugins {
  kotlin("multiplatform")
  id("ru.vyarus.animalsniffer").apply(false)
  id("org.jetbrains.dokka").apply(false)
  id("com.vanniktech.maven.publish.base").apply(false)
}

if (project.rootProject.name == "wire") {
  apply(plugin = "ru.vyarus.animalsniffer")
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "com.vanniktech.maven.publish.base")
}

kotlin {
  jvm {
    withJava()
  }
  if (System.getProperty("kjs", "true").toBoolean()) {
    js {
      configure(listOf(compilations.getByName("main"), compilations.getByName("test"))) {
        tasks.getByName(compileKotlinTaskName) {
          kotlinOptions {
            moduleKind = "umd"
            sourceMap = true
            metaInfo = true
          }
        }
      }
      nodejs()
      browser()
    }
  }
  if (System.getProperty("knative", "true").toBoolean()) {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // Required to generate tests tasks: https://youtrack.jetbrains.com/issue/KT-26547
    linuxX64()
    macosX64()
    macosArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.wireRuntime)
        api(libs.okio.core)
        api(libs.kotlin.coroutines.core)
      }
    }
    val jvmMain by getting {
      dependencies {
        api(libs.okhttp.core)
      }
    }
    if (System.getProperty("knative", "true").toBoolean()) {
      val nativeMain by creating {
        dependsOn(commonMain)
      }
      val iosX64Main by getting
      val iosArm64Main by getting
      val iosSimulatorArm64Main by getting
      val linuxX64Main by getting
      val macosX64Main by getting
      val macosArm64Main by getting
      val tvosX64Main by getting
      val tvosArm64Main by getting
      val tvosSimulatorArm64Main by getting
      for (it in listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main, linuxX64Main, macosX64Main, macosArm64Main, tvosX64Main, tvosArm64Main, tvosSimulatorArm64Main)) {
        it.dependsOn(nativeMain)
      }
    }
  }
  targets.all {
    compilations.all {
      kotlinOptions {
        freeCompilerArgs += "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs += "-Xexperimental=com.squareup.wire.WireGrpcExperimental"
      }
    }
  }
}

// TODO(egorand): Remove when https://github.com/srs/gradle-node-plugin/issues/301 is fixed
repositories.whenObjectAdded {
  if (this is IvyArtifactRepository) {
    metadataSources {
      artifact()
    }
  }
}

if (project.rootProject.name == "wire") {
  val main by sourceSets.getting
  configure<AnimalSnifferExtension> {
    sourceSets = listOf(main)
    ignore("com.squareup.wire.internal")
  }

  configure<MavenPublishBaseExtension> {
    configure(
      KotlinMultiplatform(javadocJar = Dokka("dokkaGfm"))
    )
  }
}
