package com.example.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object PhotoHelper {

    /**
     * Saves a captured Bitmap to internal application storage and returns the local file path.
     */
    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        stickerText: String? = null,
        captionText: String? = null,
        checkpointId: Int = 0
    ): String {
        return try {
            val photosDir = File(context.filesDir, "party_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val fileName = "photo_cp_${checkpointId}_${System.currentTimeMillis()}.jpg"
            val file = File(photosDir, fileName)

            // If sticker or caption is present, render onto bitmap
            val processedBitmap = if (!stickerText.isNullOrBlank() || !captionText.isNullOrBlank()) {
                overlayStickerAndCaption(bitmap, stickerText, captionText)
            } else {
                bitmap
            }

            FileOutputStream(file).use { out ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            if (processedBitmap != bitmap && !processedBitmap.isRecycled) {
                processedBitmap.recycle()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Copies a Uri selected from Gallery into internal application storage and returns the local file path.
     */
    fun saveUriToInternalStorage(
        context: Context,
        uri: Uri,
        stickerText: String? = null,
        captionText: String? = null,
        checkpointId: Int = 0
    ): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                saveBitmapToInternalStorage(context, originalBitmap, stickerText, captionText, checkpointId)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Burns sticker badge and caption onto the photo for polaroid style rendering.
     */
    private fun overlayStickerAndCaption(
        source: Bitmap,
        stickerText: String?,
        captionText: String?
    ): Bitmap {
        val mutableBitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Sticker pill at top-right or top-left
        if (!stickerText.isNullOrBlank()) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#7C3AED") // PurplePrimary
                style = Paint.Style.FILL
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (height * 0.042f).coerceIn(24f, 60f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val textWidth = textPaint.measureText(stickerText)
            val padding = 20f
            val pillRect = RectF(
                24f,
                24f,
                24f + textWidth + (padding * 2),
                24f + textPaint.textSize + (padding * 1.5f)
            )
            canvas.drawRoundRect(pillRect, 20f, 20f, bgPaint)
            canvas.drawText(stickerText, pillRect.left + padding, pillRect.bottom - (padding * 0.7f), textPaint)
        }

        // Caption bar at bottom if present
        if (!captionText.isNullOrBlank()) {
            val barHeight = (height * 0.12f).coerceIn(60f, 140f)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 0, 0, 0)
                style = Paint.Style.FILL
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (height * 0.038f).coerceIn(22f, 52f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            canvas.drawRect(0f, height - barHeight, width, height, bgPaint)
            canvas.drawText(captionText, 24f, height - (barHeight * 0.35f), textPaint)
        }

        return mutableBitmap
    }

    /**
     * Creates a high quality festive commemorative snapshot if user takes photo in simulation / test mode.
     */
    fun createFestivePlaceholderBitmap(
        landmarkName: String,
        teamName: String,
        teamEmoji: String,
        stickerText: String
    ): Bitmap {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient background
        val isAitor = teamName.contains("AITOR", ignoreCase = true) || teamEmoji.contains("🦁")
        val bgPaint = Paint().apply {
            color = if (isAitor) Color.parseColor("#4C1D95") else Color.parseColor("#9F1239")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Decorative inner frame
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        canvas.drawRoundRect(RectF(30f, 30f, width - 30f, height - 30f), 32f, 32f, borderPaint)

        // Emoji in center
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 140f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(teamEmoji, width / 2f, height / 2f - 40f, emojiPaint)

        // Landmark name
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(landmarkName, width / 2f, height / 2f + 60f, textPaint)

        // Subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FDE047") // Gold
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Despedida Aitor & Amaia • León", width / 2f, height / 2f + 110f, subPaint)

        // Sticker pill
        val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(stickerText, width / 2f, height - 80f, stickerPaint)

        return bitmap
    }

    /**
     * Exports and downloads the photo directly into the Android System Gallery / MediaStore (Pictures/DespedidaLeon).
     */
    fun savePhotoToSystemGallery(
        context: Context,
        filePathOrUri: String,
        landmarkTitle: String
    ): Boolean {
        return try {
            val bitmap = if (filePathOrUri.startsWith("content://") || filePathOrUri.startsWith("file://")) {
                val uri = Uri.parse(filePathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                val file = File(filePathOrUri)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            }

            if (bitmap == null) {
                Toast.makeText(context, "No se pudo cargar la imagen original.", Toast.LENGTH_SHORT).show()
                return false
            }

            val sanitizedTitle = landmarkTitle.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
            val fileName = "Despedida_Leon_${sanitizedTitle}_${System.currentTimeMillis()}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/DespedidaLeon")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = context.contentResolver.insert(collection, contentValues)
            if (itemUri != null) {
                context.contentResolver.openOutputStream(itemUri)?.use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(itemUri, contentValues, null, null)
                }

                Toast.makeText(
                    context,
                    "🎉 ¡Foto de '$landmarkTitle' guardada en tu Galería de fotos!",
                    Toast.LENGTH_LONG
                ).show()
                true
            } else {
                Toast.makeText(context, "Error al crear entrada en la Galería.", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error guardando en galería: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
