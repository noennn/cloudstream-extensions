// Numero de version del plugin. Incrementalo cada vez que cambies el codigo,
// si no CloudStream no detectara la actualizacion.
version = 1

cloudstream {
    description = "Doramas con subtitulos en Espanol y Latino (doramasyt.com)"
    authors = listOf("noennn")
    // 0: Down | 1: Ok | 2: Slow | 3: Beta
    status = 1
    language = "mx"
    tvTypes = listOf("AsianDrama")
    iconUrl = "https://www.google.com/s2/favicons?domain=doramasyt.com&sz=%size%"
}

// Namespace unico de este modulo (el default del root es com.noennn.cs)
extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.doramasyt"
}
