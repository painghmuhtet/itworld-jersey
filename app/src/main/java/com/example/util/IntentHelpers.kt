package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.data.JerseyCollarStyle
import com.example.data.JerseyProduct
import com.example.data.StoreConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object IntentHelpers {

    private val downloadClient = OkHttpClient()

    fun openWhatsAppOrder(context: Context, product: JerseyProduct) {
        val pid = product.productId.ifBlank { "Please confirm" }
        val message = buildString {
            appendLine("Hello IT WORLD JERSEY! I would like to order this product.")
            appendLine()
            appendLine("Product Name: ${product.productName}")
            appendLine("Design: ${product.label}")
            appendLine("Product ID: $pid")
            appendLine("Image: ${product.imageUrl}")
            appendLine()
            append("Please confirm price and availability. Thank you!")
        }
        openWhatsAppMessage(context, message)
    }

    fun openWhatsAppCustomQuote(
        context: Context,
        teamName: String,
        playerName: String,
        playerNumber: Int,
        style: JerseyCollarStyle,
        primaryHex: String,
        accentHex: String,
        shortsHex: String,
        customerPhone: String,
        notes: String
    ) {
        val msg = buildString {
            appendLine("IT WORLD JERSEY — CUSTOM DESIGN QUOTE")
            appendLine("Team: ${teamName.trim().ifBlank { "Not specified" }}")
            appendLine("Player name: ${playerName.trim().ifBlank { "Not specified" }}")
            appendLine("Number: $playerNumber")
            appendLine("Jersey style: ${style.label}")
            appendLine("Primary colour: $primaryHex")
            appendLine("Accent colour: $accentHex")
            appendLine("Shorts colour: $shortsHex")
            appendLine("Customer WhatsApp: ${customerPhone.trim()}")
            appendLine("Extra notes: ${notes.trim()}")
            append("Please quote this custom kit concept. Preview file can be shared separately.")
        }
        openWhatsAppMessage(context, msg)
    }

    fun openWhatsAppMessage(context: Context, message: String) {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name()).replace("+", "%20")
        val url = "https://wa.me/${StoreConstants.WHATSAPP_NUMBER}?text=$encoded"
        openUrl(context, url)
    }

    fun openEmail(context: Context, email: String, subject: String = "IT WORLD JERSEY Enquiry") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}")
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Send email to $email"))
        }.onFailure {
            Toast.makeText(context, "No email app found for $email", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPhoneDialer(context: Context, phone: String = "+601126238907") {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        runCatching { context.startActivity(intent) }
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun saveImageUrlToGallery(context: Context, url: String, label: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val bytes = downloadClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) error("Download failed (${resp.code})")
                    resp.body?.bytes() ?: error("Empty image data")
                }
                val safeName = label.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val fileName = "${safeName.ifBlank { "jersey-design" }}.webp"
                saveBytesToPictures(context, bytes, fileName, "image/webp")
                Result.success(fileName)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveCustomKitPreviewToGallery(
        context: Context,
        teamName: String,
        playerName: String,
        playerNumber: Int,
        style: JerseyCollarStyle,
        primaryColor: Color,
        accentColor: Color,
        shortsColor: Color,
        isBackView: Boolean
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = renderKitBitmap(
                    teamName = teamName,
                    playerName = playerName,
                    playerNumber = playerNumber,
                    style = style,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    shortsColor = shortsColor,
                    isBackView = isBackView
                )
                val fileName = "it-world-jersey-concept-${System.currentTimeMillis()}.png"
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ITWorldJersey")
                    }
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Could not create MediaStore entry")
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: error("Could not write image file")
                bitmap.recycle()
                Result.success(fileName)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun saveBytesToPictures(
        context: Context,
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ITWorldJersey")
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to save image")
        resolver.openOutputStream(uri)?.use { out ->
            out.write(bytes)
        } ?: error("Unable to open output stream")
    }

    private fun renderKitBitmap(
        teamName: String,
        playerName: String,
        playerNumber: Int,
        style: JerseyCollarStyle,
        primaryColor: Color,
        accentColor: Color,
        shortsColor: Color,
        isBackView: Boolean
    ): Bitmap {
        val w = 900
        val h = 1040
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0xFF0B1428.toInt(), 0xFF0A1020.toInt(), 0xFF1B1030.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 36f, 36f, bgPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("IT WORLD JERSEY — CUSTOM CONCEPT", w / 2f, 56f, headerPaint)

        // Draw shirt scaled from 300x330 SVG coordinates into [180..720, 80..674]
        c.save()
        c.translate(180f, 80f)
        c.scale(1.8f, 1.8f)

        val shirtPath = Path().apply {
            moveTo(95f, 36f)
            lineTo(119f, 22f)
            quadTo(150f, 40f, 181f, 22f)
            lineTo(205f, 36f)
            lineTo(263f, 73f)
            lineTo(231f, 125f)
            lineTo(204f, 109f)
            lineTo(204f, 300f)
            quadTo(150f, 316f, 96f, 300f)
            lineTo(96f, 109f)
            lineTo(69f, 125f)
            lineTo(37f, 73f)
            close()
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            color = primaryColor.toArgb()
        }
        c.drawPath(shirtPath, fillPaint)

        val stripePath = Path().apply {
            moveTo(116f, 35f)
            lineTo(142f, 303f)
            lineTo(161f, 303f)
            lineTo(184f, 35f)
            quadTo(150f, 47f, 116f, 35f)
            close()
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            color = accentColor.toArgb()
            alpha = 242
        }
        c.drawPath(stripePath, accentPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            color = 0xFFE5E7EB.toInt()
            strokeWidth = 3f
            strokeJoin = Paint.Join.ROUND
        }
        c.drawPath(shirtPath, strokePaint)

        val collarStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            color = accentColor.toArgb()
            strokeWidth = if (style == JerseyCollarStyle.ROUND_NECK) 10f else 4f
            strokeJoin = Paint.Join.ROUND
        }
        val collarFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            color = 0xFF111827.toInt()
        }
        when (style) {
            JerseyCollarStyle.CLASSIC_V_NECK -> {
                val p = Path().apply {
                    moveTo(119f, 24f); quadTo(150f, 57f, 181f, 24f)
                    lineTo(166f, 63f); lineTo(150f, 77f); lineTo(134f, 63f); close()
                }
                c.drawPath(p, collarFill)
                c.drawPath(p, collarStroke)
            }
            JerseyCollarStyle.ROUND_NECK -> {
                val p = Path().apply { moveTo(123f, 28f); quadTo(150f, 50f, 177f, 28f) }
                c.drawPath(p, collarStroke)
            }
            JerseyCollarStyle.POLO_COLLAR -> {
                val p = Path().apply {
                    moveTo(119f, 24f); lineTo(138f, 49f); lineTo(150f, 39f); lineTo(162f, 49f)
                    lineTo(181f, 24f); lineTo(173f, 63f); lineTo(150f, 76f); lineTo(127f, 63f); close()
                }
                c.drawPath(p, collarFill)
                c.drawPath(p, collarStroke)
            }
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, 0xAA000000.toInt())
        }
        val topLabel = if (isBackView) {
            playerName.trim().ifBlank { "YOUR NAME" }.uppercase().take(16)
        } else {
            teamName.trim().ifBlank { "YOUR TEAM" }.uppercase().take(22)
        }
        textPaint.textSize = 16f
        c.drawText(topLabel, 150f, if (isBackView) 120f else 157f, textPaint)
        textPaint.textSize = 56f
        c.drawText(playerNumber.coerceIn(0, 99).toString(), 150f, 224f, textPaint)
        textPaint.textSize = 9f
        c.drawText("CUSTOM KIT CONCEPT", 150f, 281f, textPaint)
        c.restore()

        // Draw shorts scaled from 260x190 SVG into bottom area
        c.save()
        c.translate(230f, 665f)
        c.scale(1.7f, 1.7f)
        val shortsPath = Path().apply {
            moveTo(44f, 18f); quadTo(130f, 31f, 216f, 18f)
            lineTo(206f, 90f); lineTo(177f, 167f); lineTo(135f, 167f)
            lineTo(130f, 93f); lineTo(125f, 167f); lineTo(83f, 167f); lineTo(54f, 90f); close()
        }
        val shortsFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            color = shortsColor.toArgb()
        }
        c.drawPath(shortsPath, shortsFill)
        val shortsAccentPath = Path().apply {
            moveTo(44f, 18f); lineTo(64f, 21f); lineTo(75f, 89f); lineTo(92f, 155f); lineTo(83f, 167f); lineTo(54f, 90f); close()
            moveTo(216f, 18f); lineTo(196f, 21f); lineTo(185f, 89f); lineTo(168f, 155f); lineTo(177f, 167f); lineTo(206f, 90f); close()
        }
        c.drawPath(shortsAccentPath, accentPaint)
        c.drawPath(shortsPath, strokePaint)
        textPaint.textSize = 25f
        c.drawText(playerNumber.coerceIn(0, 99).toString(), 169f, 67f, textPaint)
        c.restore()

        return bmp
    }
}
