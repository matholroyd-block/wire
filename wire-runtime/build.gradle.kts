import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferExtension

// apply(plugin = "org.jetbrains.kotlin.multiplatform")
plugins {
  kotlin("multiplatform")
  id("com.github.gmazzo.buildconfig")
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
    all {
      languageSettings.optIn("kotlin.Experimental")
    }
    val commonMain by getting {
      dependencies {
        api(libs.okio.core)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlin.test.common)
        implementation(libs.kotlin.test.annotations)
      }
    }
    val jvmMain by getting {
      dependencies {
        compileOnly(libs.android)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.assertj)
        implementation(libs.kotlin.test.junit)
      }
    }
    if (System.getProperty("kjs", "true").toBoolean()) {
      val jsTest by getting {
        dependencies {
          implementation(libs.kotlin.test.js)
        }
      }
    }
    if (System.getProperty("knative", "true").toBoolean()) {
      val nativeMain by creating {
        dependsOn(commonMain)
      }
      val nativeTest by creating {
        dependsOn(commonTest)
      }
      val darwinMain by creating {
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
      val iosX64Test by getting
      val iosArm64Test by getting
      val iosSimulatorArm64Test by getting
      val linuxX64Test by getting
      val macosX64Test by getting
      val macosArm64Test by getting
      val tvosX64Test by getting
      val tvosArm64Test by getting
      val tvosSimulatorArm64Test by getting

      for (it in listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main, linuxX64Main, macosX64Main, macosArm64Main, tvosX64Main, tvosArm64Main, tvosSimulatorArm64Main)) {
        it.dependsOn(nativeMain)
      }

      for (it in listOf(iosX64Test, iosArm64Test, iosSimulatorArm64Test, linuxX64Test, macosX64Test, macosArm64Test, tvosX64Test, tvosArm64Test, tvosSimulatorArm64Test)) {
        it.dependsOn(nativeTest)
      }

      for (it in listOf(iosX64Main, iosArm64Main, macosX64Main, macosArm64Main, tvosX64Main, tvosArm64Main)) {
        it.dependsOn(darwinMain)
      }
    }
  }
}

afterEvaluate {
  val installLocally by tasks.creating {
    dependsOn("publishKotlinMultiplatformPublicationToTestRepository")
    dependsOn("publishJvmPublicationToTestRepository")
    if (System.getProperty("kjs", "true").toBoolean()) {
      dependsOn("publishJsPublicationToTestRepository")
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

buildConfig {
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }

  packageName("com.squareup.wire")
  buildConfigField("String", "VERSION", "\"${project.version}\"")
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
