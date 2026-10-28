// Numero de version del plugin. Incrementalo cuando cambies el codigo.
version = 1

cloudstream {
    language = "mx"
    // All of these properties are optional, you can safely remove them

    description = "Peliculas, series, animes y cartoons en Español y Español Latio"
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
        "TvSeries",
        "Anime",
        "Cartoon",
    )

    iconUrl = "https://sololatino.net/wp-content/uploads/2020/10/cropped-logo-final-192x192.png"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.sololatino"
}
