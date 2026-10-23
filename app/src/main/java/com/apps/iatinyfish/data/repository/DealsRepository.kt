package com.apps.iatinyfish.data.repository

import com.apps.iatinyfish.data.model.ProductOffer
import kotlinx.coroutines.delay

interface DealsRepository {
    val isPreviewData: Boolean

    suspend fun search(query: String): List<ProductOffer>
}

/** Datos ficticios para probar la interfaz antes de conectar Supabase Cloud. */
object PreviewDealsRepository : DealsRepository {
    override val isPreviewData: Boolean = true
    private val offers = listOf(
        ProductOffer(
            id = "pixel-store",
            store = "Pixel Store",
            productName = "Nintendo Switch OLED · Blanca",
            price = "\$329",
            originalPrice = "\$349",
            shipping = "Envío gratis",
            delivery = "Llega en 2–4 días",
            rating = "4.8",
            sourceUrl = "https://www.nintendo.com/us/gaming-systems/switch/oled-model/",
            summary = "El precio más bajo de la búsqueda. Incluye consola, Joy-Con blancos y base OLED.",
            tags = listOf("Ejemplo ficticio", "Demo"),
            isBestValue = true,
        ),
        ProductOffer(
            id = "game-hub",
            store = "Game Hub",
            productName = "Nintendo Switch OLED · Neon",
            price = "\$339",
            shipping = "Envío gratis",
            delivery = "Disponible hoy",
            rating = "4.7",
            sourceUrl = "https://www.nintendo.com/us/gaming-systems/switch/oled-model/",
            summary = "Disponible para entrega rápida. El paquete incluye una tarjeta de regalo de \$10.",
            tags = listOf("Ejemplo ficticio", "Demo"),
        ),
        ProductOffer(
            id = "tech-mart",
            store = "Tech Mart",
            productName = "Nintendo Switch OLED + Mario Kart 8",
            price = "\$379",
            originalPrice = "\$409",
            shipping = "\$5.99",
            delivery = "Llega en 3–5 días",
            rating = "4.9",
            sourceUrl = "https://www.nintendo.com/us/gaming-systems/switch/oled-model/",
            summary = "Bundle con consola OLED y Mario Kart 8 Deluxe. Conviene si también buscabas el juego.",
            tags = listOf("Ejemplo ficticio", "Demo"),
        ),
    )

    override suspend fun search(query: String): List<ProductOffer> {
        delay(700)
        if (query.isBlank()) return offers
        val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
        return offers.filter { offer ->
            val searchableText = "${offer.store} ${offer.productName} ${offer.summary} ${offer.tags.joinToString()}".lowercase()
            terms.any(searchableText::contains)
        }
    }
}
