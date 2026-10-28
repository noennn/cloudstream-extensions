// Numero de version del plugin. Incrementalo cuando cambies el codigo.
version = 3

cloudstream {
    language = "mx"
    // All of these properties are optional, you can safely remove them
    description = "Doramas asiaticas subtituladas en español."
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
        "AsianDrama",
    )

    iconUrl = "https://www.google.com/s2/favicons?domain=doramasyt.com&sz=%size%"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.doramasyt"
}
