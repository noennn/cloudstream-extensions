rootProject.name = "cloudstream-extensions"

// Incluye automaticamente TODA carpeta que tenga un build.gradle.kts.
// Para agregar un sitio nuevo: crea la carpeta y su build.gradle.kts, nada mas.
// Para desactivarlo temporalmente: agregalo a la lista `disabled`.

val disabled = listOf<String>()

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory() }?.forEach { block(it) }
}

File(rootDir, ".").eachDir { dir ->
    if (!disabled.contains(dir.name) && File(dir, "build.gradle.kts").exists()) {
        include(dir.name)
    }
}
