import com.android.build.api.dsl.LibraryExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:9.1.1")
        // Plugin oficial de CloudStream: empaqueta cada modulo en un .cs3
        classpath("com.github.recloudstream.gradle:gradle:81b1d424d")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) =
    extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: LibraryExtension.() -> Unit) {
    extensions.getByName<LibraryExtension>("android").apply {
        // Fuerza JDK 17 aunque el sistema tenga uno mas nuevo
        project.extensions.findByType(JavaPluginExtension::class.java)?.apply {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
        configuration()
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        // En GitHub Actions GITHUB_REPOSITORY = "usuario/repo"; en local usa el fallback
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "noennn/cloudstream-extensions")
        authors = listOf("noennn")
    }

    android {
        // Namespace por defecto. Sobrescribelo en el build.gradle.kts de cada modulo.
        namespace = "com.noennn.cs"
        compileSdk = 36

        defaultConfig {
            minSdk = 21
        }

        lint {
            targetSdk = 36
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<KotlinJvmCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
                freeCompilerArgs.addAll(
                    "-Xno-call-assertions",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions",
                    "-Xannotation-default-target=param-property"
                )
            }
        }
    }

    dependencies {
        val implementation by configurations
        val cloudstream by configurations

        // Stubs de las clases de CloudStream (no se empaquetan, solo compilan)
        cloudstream("com.lagradost:cloudstream3:pre-release")

        implementation(kotlin("stdlib"))
        implementation("com.github.Blatzar:NiceHttp:0.4.18")            // HTTP
        implementation("org.jsoup:jsoup:1.22.1")                        // Parser HTML
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1") // JSON
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")   // delay()
        implementation("com.github.vidstige:jadb:v1.2.1")               // Ads (opcional)
        implementation("org.mozilla:rhino:1.8.1")                       // Ejecutar JS
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
