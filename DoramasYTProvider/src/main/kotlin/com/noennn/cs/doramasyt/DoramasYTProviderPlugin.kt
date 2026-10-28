package com.noennn.cs.doramasyt

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

/**
 * Punto de entrada del plugin.
 * CloudStream busca esta clase por la anotacion @CloudstreamPlugin y lee su
 * nombre completo desde el manifest.json que genera el build.
 */
@CloudstreamPlugin
class DoramasYTProviderPlugin : Plugin() {
    override fun load(context: Context) {
        // Registra el provider. No edites la lista de providers directamente.
        registerMainAPI(DoramasYTProvider())
    }
}
