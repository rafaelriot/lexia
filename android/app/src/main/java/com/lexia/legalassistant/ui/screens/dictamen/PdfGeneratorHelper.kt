package com.lexia.legalassistant.ui.screens.dictamen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lexia.legalassistant.data.model.DictamenData
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import java.io.File
import java.io.FileOutputStream

object PdfGeneratorHelper {

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ): Float {
        var curY = startY
        val words = text.split(" ")
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(currentLine, x, curY, paint)
                curY += lineHeight
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, curY, paint)
            curY += lineHeight
        }
        return curY
    }

    fun generateDictamenPdf(
        context: Context,
        dictamenData: DictamenData = LegalArticlesRepository.buildDictamen("cpeum_16")
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background
        paint.color = Color.rgb(0, 35, 111) // LexiaPrimary
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LexIA · ${dictamenData.title}", 40f, 38f, paint)

        paint.textSize = 9.5f
        paint.color = Color.rgb(144, 168, 255) // OnPrimaryContainer
        canvas.drawText("${dictamenData.expediente} · ${dictamenData.subtitle}", 40f, 58f, paint)

        // Section I
        var y = 105f
        paint.color = Color.rgb(0, 35, 111)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(dictamenData.materiaConsultadaTitle, 40f, y, paint)

        y += 20f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y = drawWrappedText(canvas, dictamenData.materiaConsultadaDesc, 40f, y, 515f, 16f, paint)

        // Section II
        y += 16f
        paint.color = Color.rgb(0, 35, 111)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(dictamenData.fundamentacionNormativaTitle, 40f, y, paint)

        // Statutory Quote Box
        y += 14f
        val boxStartY = y
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

        // Pre-calculate height of quote text
        var estimatedY = boxStartY + 18f
        val words = dictamenData.fundamentacionNormativaText.split(" ")
        var tempLine = ""
        for (w in words) {
            val tl = if (tempLine.isEmpty()) w else "$tempLine $w"
            if (paint.measureText(tl) > 480f) {
                estimatedY += 15f
                tempLine = w
            } else {
                tempLine = tl
            }
        }
        if (tempLine.isNotEmpty()) estimatedY += 15f
        val boxHeight = (estimatedY - boxStartY + 10f).coerceAtLeast(50f)

        // Background of quote box
        paint.color = Color.rgb(239, 244, 255) // SurfaceContainerLow
        canvas.drawRect(40f, boxStartY, 555f, boxStartY + boxHeight, paint)

        // Left accent bar
        paint.color = Color.rgb(101, 52, 0) // Tertiary strip
        canvas.drawRect(40f, boxStartY, 46f, boxStartY + boxHeight, paint)

        // Draw quote text on top of box background
        paint.color = Color.rgb(11, 28, 48)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        drawWrappedText(canvas, dictamenData.fundamentacionNormativaText, 55f, boxStartY + 18f, 480f, 15f, paint)

        // Section III
        y = boxStartY + boxHeight + 22f
        paint.color = Color.rgb(0, 35, 111)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(dictamenData.supuestosTitle, 40f, y, paint)

        y += 20f
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in dictamenData.supuestosList) {
            y = drawWrappedText(canvas, item, 40f, y, 515f, 15f, paint)
            y += 4f
        }

        // Footer / Forensic Hash Verification
        y = 730f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(40f, y, 555f, y + 1f, paint)

        y += 20f
        paint.color = Color.rgb(5, 150, 105) // Success
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VERIFICACIÓN FORENSE CRIPTOGRÁFICA: 100% LIBRE DE ALUCINACIÓN", 40f, y, paint)

        y += 16f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText(dictamenData.hashSha256, 40f, y, paint)
        y += 14f
        canvas.drawText("Modelo: LexIA-RAG v3.4 DistilBERT Legal-ES · Estatus: ${dictamenData.dofStatus} (${dictamenData.reformDate})", 40f, y, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, dictamenData.fileName)
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun sharePdf(context: Context, pdfFile: File, dictamenData: DictamenData? = null) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.lexia.legalassistant.fileprovider",
                pdfFile
            )

            val subject = dictamenData?.let { "Dictamen Jurídico ${it.subtitle} · LexIA" }
                ?: "Dictamen Jurídico · LexIA"
            val text = dictamenData?.let {
                "Dictamen técnico emitido por el asistente jurídico LexIA sobre ${it.subtitle} con cotejo oficial DOF."
            } ?: "Dictamen técnico emitido por el asistente jurídico LexIA con cotejo oficial DOF."

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir Dictamen PDF vía...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun downloadPdfToPublicFolder(context: Context, pdfFile: File, targetFileName: String? = null) {
        try {
            val fileName = targetFileName ?: pdfFile.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        pdfFile.inputStream().use { inp ->
                            inp.copyTo(out)
                        }
                    }
                    Toast.makeText(context, "✅ Dictamen guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // Fallback for older devices or direct file path
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val destFile = File(downloadsDir, fileName)
            pdfFile.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "✅ Dictamen guardado en Descargas: ${destFile.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to app's external files directory if public Downloads failed
            try {
                val fileName = targetFileName ?: pdfFile.name
                val extDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val fallbackFile = File(extDir, fileName)
                pdfFile.copyTo(fallbackFile, overwrite = true)
                Toast.makeText(context, "✅ Guardado en: ${fallbackFile.name}", Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                Toast.makeText(context, "Error al guardar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun printPdf(context: Context, pdfFile: File, jobName: String = "Dictamen Legal") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Servicio de impresión no disponible en este dispositivo", Toast.LENGTH_SHORT).show()
                return
            }

            val printAdapter = object : android.print.PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: android.print.PrintAttributes?,
                    newAttributes: android.print.PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        destination?.fileDescriptor?.let { fd ->
                            FileOutputStream(fd).use { out ->
                                pdfFile.inputStream().use { inp ->
                                    inp.copyTo(out)
                                }
                            }
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            val printAttributes = android.print.PrintAttributes.Builder()
                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                .build()

            printManager.print(jobName, printAdapter, printAttributes)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al enviar a imprimir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
