# CloudStream Extensions — `noennn`

Repositorio de **extensiones (plugins) para CloudStream 3** con sitios web propios.

Cada carpeta de primer nivel que tenga un `build.gradle.kts` se compila
automaticamente en su propio archivo `.cs3` y se publica en la rama `builds`.

---

## Providers incluidos

Todos en espanol, **sin porno** y sin providers en ingles.

| Provider | Sitio | Tipo | Idioma |
|---|---|---|---|
| `DoramasYTProvider` | doramasyt.com | Doramas asiaticas | mx |
| `LatAnimeProvider` | latanime.org | Anime | mx |
| `PeliculasFlixProvider` | peliculasflix.co | Peliculas | es |
| `SoloLatinoProvider` | sololatino.net | Peliculas, series, anime | mx |
| `CinecalidadProvider` | cinecalidad.lol | Peliculas, series | mx |
| `Pelisplus4KProvider` | pelisplus.to | Peliculas, series | mx |

---

## Como se instala en CloudStream

### Opcion rapida: APK parcheado (sin tocar nada en la app)

El APK parcheado ya trae este repositorio **y** el oficial de CloudStream
preinstalados, y descarga e instala los plugins **solo al arrancar**. No
tienes que añadir repos ni instalar nada a mano.

```
https://github.com/noennn/cloudstream-extensions/releases
```

1. Instala el `.apk` parcheado (desinstala el oficial antes).
2. Abre la app: los plugins se descargan solos en el primer arranque.
3. Listo.

### Opcion normal: añadir el repositorio a mano

1. Abre CloudStream → **Ajustes → Extensiones → Añadir repositorio**
2. Pega esta URL:

```
https://raw.githubusercontent.com/noennn/cloudstream-extensions/builds/repo.json
```

3. Entra al repositorio, busca el provider que quieras → **Instalar**
4. En la app, abre el engranaje del plugin para seleccionar el proveedor

> El repositorio se actualiza solo: cada vez que se hace push a `main`,
> GitHub Actions recompila y sube los `.cs3` a la rama `builds`.

---

## Como agregar un sitio nuevo

### Opcion rapida: el script

```bash
./nuevo-proveedor.sh MiSitio https://misitio.com es
```

Crea la carpeta completa con los 4 archivos y los selectores CSS de ejemplo.
Solo tienes que ajustar los selectores al HTML real del sitio y hacer push.

### Opcion manual

Solo hay que crear **una carpeta**. `settings.gradle.kts` la incluye sola.

```
MiSitioProvider/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    └── kotlin/com/noennn/cs/misitio/
        ├── MiSitioProvider.kt
        └── MiSitioProviderPlugin.kt
```

### 1. `MiSitioProvider/build.gradle.kts`

```kotlin
// Sube este numero cuando cambies el codigo, si no CloudStream no actualiza.
version = 1

cloudstream {
    description = "Descripcion corta que se ve en la lista de extensiones"
    authors = listOf("noennn")
    status = 1          // 0: Caido | 1: Ok | 2: Lento | 3: Beta
    language = "es"     // "es", "mx", "en"...
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=misitio.com&sz=%size%"
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.noennn.cs.misitio"
}
```

### 2. `MiSitioProvider/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

### 3. `MiSitioProviderPlugin.kt` — el punto de entrada

```kotlin
package com.noennn.cs.misitio

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class MiSitioProviderPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(MiSitioProvider())
    }
}
```

### 4. `MiSitioProvider.kt` — el provider

```kotlin
package com.noennn.cs.misitio

import com.lagradost.cloudstream3.*

class MiSitioProvider : MainAPI() {
    override var mainUrl = "https://misitio.com"
    override var name = "MiSitio"
    override var lang = "es"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    // Homepage: las secciones que se ven al abrir la app
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = app.get("$mainUrl/peliculas").document.select("div.movie").mapNotNull { el ->
            val title = el.selectFirst("h2")?.text() ?: return@mapNotNull null
            val href = el.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = el.selectFirst("img")?.attr("src")
            newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                posterUrl = poster?.let(::fixUrl)
            }
        }
        return newHomePageResponse(listOf(HomePageList("Peliculas", items)))
    }

    // Buscador
    override suspend fun search(query: String): List<SearchResponse> {
        return app.get("$mainUrl/buscar?q=${query.urlEncode()}")
            .document.select("div.movie").mapNotNull { el ->
                val title = el.selectFirst("h2")?.text() ?: return@mapNotNull null
                val href = el.selectFirst("a")?.attr("href") ?: return@mapNotNull null
                val poster = el.selectFirst("img")?.attr("src")
                newMovieSearchResponse(title, fixUrl(href), TvType.Movie) {
                    posterUrl = poster?.let(::fixUrl)
                }
            }
    }

    // Detalle + capitulos
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: throw ErrorLoadingException()
        val poster = doc.selectFirst("img.poster")?.attr("src")
        val eps = doc.select("ul.episodes a").map { a ->
            Episode(fixUrl(a.attr("href")), a.text())
        }
        return newMovieLoadResponse(title, url, TvType.Movie, eps) {
            posterUrl = poster?.let(::fixUrl)
        }
    }

    // Servidores de video: aqui se resuelve el embed a un link directo
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val html = app.get(data).document
        val embed = html.selectFirst("iframe")?.attr("src") ?: return false
        loadExtractor(embed, mainUrl, subtitleCallback, callback)
        return true
    }
}
```

### 5. Haz push

```bash
git add .
git commit -m "Agrega MiSitioProvider"
git push
```

GitHub Actions compila y publica solo. En 1-2 minutos el plugin aparece
en CloudStream.

---

## Como funciona el build (lo que se copio de storm-ext)

### Flujo

```
master (codigo Kotlin)
   |
   |  GitHub Actions: ./gradlew make makePluginsJson
   v
builds/  (repo.json + plugins.json + *.cs3)
   |
   v
CloudStream lee repo.json -> plugins.json -> descarga el .cs3
```

### Piezas

| Pieza | Que hace |
|---|---|
| `com.github.recloudstream.gradle:gradle` | Plugin oficial de CloudStream. Registra las tareas `make` y `makePluginsJson`, y empaqueta cada modulo en un `.cs3`. |
| `build.gradle.kts` (root) | Aplica `com.android.library` + el plugin de CloudStream a **todos** los modulos, y define las dependencias comunes (NiceHttp, jsoup, jackson, rhino). |
| `setRepo(...)` | Le dice al plugin a que repo publicar. En CI usa `$GITHUB_REPOSITORY`; en local usa el fallback. |
| `settings.gradle.kts` | Incluye auto cualquier carpeta con `build.gradle.kts`. Agregar un provider = crear la carpeta. |
| `make` | Compila cada modulo y genera `<Modulo>/build/<Modulo>.cs3`. |
| `makePluginsJson` | Junta los metadatos de cada `.cs3` en `build/plugins.json`. |
| `.github/workflows/build.yml` | Compila en `ubuntu-latest` (JDK 17 + Android SDK 36) y sube el resultado a la rama `builds`. |

### Que es un `.cs3`

Un ZIP con dos archivos:

```
manifest.json   ->  { "pluginClassName": "...", "name": "...", "version": 7, "requiresResources": false }
classes.dex     ->  el codigo Kotlin compilado
```

`manifest.json` lo genera el plugin de gradle a partir de la clase anotada
con `@CloudstreamPlugin`.

### Compilar en local

```bash
export JAVA_HOME=/ruta/al/jdk-17
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew make makePluginsJson
# salida: DoramasYTProvider/build/DoramasYTProvider.cs3
#          build/plugins.json
```

---

## Referencia rapida del API

| Metodo | Cuando se llama | Que devuelve |
|---|---|---|
| `getMainPage(page, request)` | Al abrir la app / cambiar de pestana | `HomePageResponse` con listas de contenido |
| `search(query)` | Al buscar | `List<SearchResponse>` |
| `load(url)` | Al abrir una pelicula/serie | `LoadResponse` con poster, sinopsis, generos, capitulos |
| `loadLinks(data, ...)` | Al pulsar Reproducir | Links de video via `loadExtractor()` |
| `getQuickSearch(query)` | Busqueda rapida (opcional) | `List<SearchResponse>` |

**Tipos utiles:** `TvType.Movie`, `TvType.TvSeries`, `TvType.AsianDrama`,
`TvType.Anime`, `TvType.OVA`, `TvType.Cartoon`, `TvType.Live`,
`TvType.Documentary`.

**Constructores de respuesta:**
`newMovieSearchResponse()`, `newTvSeriesSearchResponse()`,
`newAnimeSearchResponse()`, `newLiveStreamSearchResponse()`,
`newMovieLoadResponse()`, `newTvSeriesLoadResponse()`,
`newAnimeLoadResponse()`, `newEpisode()`.

**Herramientas:**
`app.get(url)`, `app.post(url)`, `app.get(url).parsed<T>()` (JSON),
`.document.select("css")` (jsoup), `fixUrl()`, `base64Decode()`,
`loadExtractor()`.

---

## Notas

- El paquete Kotlin de cada provider **debe** ser unico en el repo
  (usa `com.noennn.cs.<sitio>`).
- La clase anotada con `@CloudstreamPlugin` tambien debe tener nombre unico.
- Si un sitio cambia su HTML, el provider deja de servir. Es normal: por eso
  `status` existe (`2` = lento, `0` = caido).
- Los dominios de embed cambian seguido. `loadExtractor` ya trae extractores
  para `streamwish`, `vidhide`, `filemoon`, `monoschinos2` y otros; si alguno
  muere, redirigelo en `customLoadExtractor` como hace el ejemplo de DoramasYT.

---

## Creditos y licencia

- Sistema de plugins y `.cs3`: [recloudstream/cloudstream](https://github.com/recloudstream/cloudstream) — GPL-3.0
- Plugin de gradle: [recloudstream/gradle](https://github.com/recloudstream/gradle)
- Provider de ejemplo `DoramasYT`: adaptado de
  [Stormunblessed/cloudstream-3](https://github.com/Stormunblessed/cloudstream-3) — GPL-3.0
- Estructura del repo y workflow de build: adaptados de
  [redblacker8/storm-ext](https://github.com/redblacker8/storm-ext)

Este repo se distribuye bajo **GPL-3.0** (ver `LICENSE`).
