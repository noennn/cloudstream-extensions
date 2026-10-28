// Numero de version del plugin. Incrementalo cuando cambies el codigo.
version = 1

cloudstream {
    language = "es"
    // All of these properties are optional, you can safely remove them
    description = "Peliculas en español (HD)."
    authors = listOf("noennn")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "Movie",
    )

    iconUrl = "https://peliculasflix.co/img/logo.png"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.peliculasflix"
}
