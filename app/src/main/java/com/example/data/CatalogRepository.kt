package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class CatalogRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    @Volatile
    private var cachedProducts: List<JerseyProduct>? = null

    suspend fun getAllProducts(): List<JerseyProduct> {
        cachedProducts?.let { return it }
        return withContext(Dispatchers.IO) {
            val rawText = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
            val rawList = json.decodeFromString<List<RawCatalogItem>>(rawText)
            val mapped = rawList.mapIndexed { index, item ->
                JerseyProduct(
                    id = index + 1,
                    key = item.key,
                    designNumber = item.n,
                    variant = item.variant,
                    label = item.label,
                    category = item.category,
                    imageUrl = encodeR2Key(item.key),
                    price = null
                )
            }
            cachedProducts = mapped
            mapped
        }
    }

    suspend fun getCategories(): List<String> {
        return getAllProducts().map { it.category }.distinct()
    }

    suspend fun getFeaturedBanners(): List<JerseyProduct> {
        return getAllProducts().filter { it.variant.isEmpty() }.take(5)
    }

    suspend fun getLiveSalePicks(): List<JerseyProduct> {
        val all = getAllProducts()
        val wanted = listOf("Design 0001", "Design 0001 A", "Design 0001 B", "Design 0002")
        return wanted.mapNotNull { label -> all.firstOrNull { it.label == label } }
            .ifEmpty { all.take(4) }
    }

    suspend fun getLatestProducts(): List<JerseyProduct> {
        val all = getAllProducts()
        val wanted = listOf("Design 1097", "Design 1098", "Design 1099", "Design 1100")
        return wanted.mapNotNull { label -> all.firstOrNull { it.label == label } }
            .ifEmpty { all.takeLast(4) }
    }
}
