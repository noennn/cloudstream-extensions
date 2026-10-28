#!/usr/bin/env bash
#
# Crea un provider nuevo (carpeta + archivos base) para un sitio web.
#
#   ./nuevo-proveedor.sh MiSitio https://misitio.com
#   ./nuevo-proveedor.sh PeliculasHD https://peliculashd.tv es
#
# Genera:
#   MiSitioProvider/build.gradle.kts
#   MiSitioProvider/src/main/AndroidManifest.xml
#   MiSitioProvider/src/main/kotlin/com/noennn/cs/misitio/MiSitioProvider.kt
#   MiSitioProvider/src/main/kotlin/com/noennn/cs/misitio/MiSitioProviderPlugin.kt
#
set -euo pipefail

NOMBRE="${1:-}"
URL="${2:-}"
LANG_ID="${3:-es}"

if [[ -z "$NOMBRE" || -z "$URL" ]]; then
    echo "Uso: $0 <Nombre> <url> [idioma]" >&2
    echo "Ej: $0 MiSitio https://misitio.com es" >&2
    exit 1
fi

# Nombre de clase: CamelCase sin espacios ni acentos.
CLASE=$(echo "$NOMBRE" | sed -E 's/[^A-Za-z0-9 ]//g; s/ +//g')
[[ -z "$CLASE" ]] && { echo "Nombre de clase invalido: $NOMBRE" >&2; exit 1; }

# Paquete: minusculas sin acentos.
PAQUETE=$(echo "$NOMBRE" | tr 'ÁÉÍÓÚáéíóúÑñ' 'AEIOUaeiouNn' | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]//g')
[[ -z "$PAQUETE" ]] && { echo "Paquete invalido: $NOMBRE" >&2; exit 1; }

DIR="${CLASE}Provider"
SRC="src/main/kotlin/com/noennn/cs/${PAQUETE}"

if [[ -d "$DIR" ]]; then
    echo "ERROR: ya existe la carpeta $DIR" >&2
    exit 1
fi

mkdir -p "$DIR/$SRC"

cat > "$DIR/build.gradle.kts" <<EOF
// Sube este numero cuando cambies el codigo, si no CloudStream no actualiza.
version = 1

cloudstream {
    description = "$CLASE - descripcion corta que se vera en la app"
    authors = listOf("noennn")
    // 0: Caido | 1: Ok | 2: Lento | 3: Beta
    status = 1
    language = "$LANG_ID"
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=$(echo "$URL" | sed -E 's|https?://(www\.)?||; s|/.*||')&sz=%size%"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.$PAQUETE"
}
EOF

cat > "$DIR/src/main/AndroidManifest.xml" <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
EOF

cat > "$DIR/$SRC/${CLASE}ProviderPlugin.kt" <<EOF
package com.noennn.cs.$PAQUETE

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class ${CLASE}ProviderPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(${CLASE}Provider())
    }
}
EOF

cat > "$DIR/$SRC/${CLASE}Provider.kt" <<EOF
package com.noennn.cs.$PAQUETE

import com.lagradost.cloudstream3.*

class ${CLASE}Provider : MainAPI() {
    override var mainUrl = "$URL"
    override var name = "$CLASE"
    override var lang = "$LANG_ID"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    // TODO: revisa los selectores CSS con la pagina real del sitio.
    //  app.get(url).document   ->  HTML parseado con jsoup (select("clase css"))

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = app.get("\$mainUrl/").document.select("div.item").mapNotNull { el ->
            val title = el.selectFirst("h2")?.text() ?: return@mapNotNull null
            val href = el.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = el.selectFirst("img")?.attr("src")
            newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                posterUrl = poster?.let(::fixUrl)
            }
        }
        if (items.isEmpty()) throw ErrorLoadingException()
        return newHomePageResponse(listOf(HomePageList("Peliculas", items)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("\$mainUrl/buscar?q=\${query.urlEncode()}")
            .document.select("div.item").mapNotNull { el ->
                val title = el.selectFirst("h2")?.text() ?: return@mapNotNull null
                val href = el.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val poster = el.selectFirst("img")?.attr("src")
                newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                    posterUrl = poster?.let(::fixUrl)
                }
            }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: throw ErrorLoadingException()
        val poster = doc.selectFirst("img.poster")?.attr("src")
        val eps = doc.select("ul.episodes a").map { a ->
            Episode(fixUrl(a.attr("href")), a.text())
        }
        return newMovieLoadResponse(title, url, TvType.Movie, eps) {
            posterUrl = poster?.let(::fixUrl)
            plot = doc.selectFirst("p.description")?.text()
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val embed = app.get(data).document.selectFirst("iframe")?.attr("src") ?: return false
        loadExtractor(embed, mainUrl, subtitleCallback, callback)
        return true
    }
}
EOF

chmod +x "$0"

echo ""
echo "Creado: $DIR"
echo "  Paquete : com.noennn.cs.$PAQUETE"
echo "  Website : $URL"
echo ""
echo "Siguientes pasos:"
echo "  1. Edita los selectores CSS en $DIR/$SRC/${CLASE}Provider.kt"
echo "  2. git add . && git commit -m 'Agrega $DIR'"
echo "  3. git push      (GitHub Actions compila y publica solo)"
echo ""
