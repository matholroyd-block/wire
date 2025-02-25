import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
  id("java-library")
  kotlin("jvm")
  id("org.jetbrains.dokka").apply(false)
  id("com.vanniktech.maven.publish.base").apply(false)
}

if (project.rootProject.name == "wire") {
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "com.vanniktech.maven.publish.base")
}

dependencies {
  api(projects.wireSchema)
  implementation(projects.wireRuntime)
  implementation(projects.wireGrpcClient)
  implementation(projects.wireGrpcServerGenerator)
  implementation(libs.okio.core)
  api(libs.kotlinpoet)
  implementation(libs.guava)
  testImplementation(projects.wireTestUtils)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.truth)
}

if (project.rootProject.name == "wire") {
  configure<MavenPublishBaseExtension> {
    configure(
      KotlinJvm(javadocJar = Dokka("dokkaGfm"), sourcesJar = true)
    )
  }
}
