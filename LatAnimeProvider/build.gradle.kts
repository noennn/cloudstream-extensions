// Numero de version del plugin. Incrementalo cuando cambies el codigo.
version = 1

cloudstream {
    language = "mx"
    // All of these properties are optional, you can safely remove them

    description = "Anime en latino y subtitulado."
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
        "Anime",
        "OVA",
    )

    iconUrl = "https://latanime.org/public/img/logito.png"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.latanime"
}
