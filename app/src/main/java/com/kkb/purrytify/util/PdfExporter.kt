package com.kkb.purrytify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.kkb.purrytify.formatTime
import com.kkb.purrytify.viewmodel.MonthlySoundCapsule
import com.kkb.purrytify.viewmodel.ProfileStatsUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

object PdfExporter {
    private val TAG = "PdfExporter"
    private val WHITE = BaseColor(255, 255, 255)
    private val LIGHT_GRAY = BaseColor(200, 200, 200)
    private val DARK_GRAY = BaseColor(60, 60, 60)
    private val DARKER_GRAY = BaseColor(40, 40, 40)
    private val BLACK = BaseColor(0, 0, 0)

    private val FONT_TITLE = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BLACK)
    private val FONT_SUBTITLE = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BLACK)
    private val FONT_NORMAL = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BLACK)
    private val FONT_SMALL = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BLACK)

    fun exportSoundCapsuleToPdf(
        context: Context,
        statsState: ProfileStatsUiState,
        onComplete: (Uri?) -> Unit
    ) {
        thread {
            var fileUri: Uri? = null
            try {
                Log.d(TAG, "Starting PDF export")

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "purrytify_sound_capsule_$timeStamp.pdf"
                val pdfFile = File(context.cacheDir, fileName)

                Log.d(TAG, "Creating file at: ${pdfFile.absolutePath}")

                val document = Document()
                try {
                    val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
                    document.open()

                    addContent(document, statsState)

                    document.close()
                    writer.close()

                    Log.d(TAG, "PDF created successfully")

                    fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        pdfFile
                    )

                    Log.d(TAG, "FileProvider URI obtained: $fileUri")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating PDF document", e)
                    if (document.isOpen) document.close()
                    throw e
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in PDF export", e)
                fileUri = null
            } finally {
                onComplete(fileUri)
            }
        }
    }

    private fun addContent(document: Document, statsState: ProfileStatsUiState) {
        document.pageSize = PageSize.A4
        document.addCreationDate()
        document.addAuthor("Purrytify App")
        document.addCreator("Purrytify Sound Capsule")

        val titleParagraph = Paragraph("PURRYTIFY SOUND CAPSULE", FONT_TITLE)
        titleParagraph.alignment = Element.ALIGN_CENTER
        titleParagraph.spacingAfter = 20f
        document.add(titleParagraph)

        val dateParagraph = Paragraph("Generated on: ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())}", FONT_SMALL)
        dateParagraph.alignment = Element.ALIGN_CENTER
        dateParagraph.spacingAfter = 30f
        document.add(dateParagraph)

        if (statsState.monthlyCapsules.isEmpty()) {
            val noCapsulesParagraph = Paragraph("No Sound Capsule data available", FONT_NORMAL)
            noCapsulesParagraph.alignment = Element.ALIGN_CENTER
            document.add(noCapsulesParagraph)
        } else {
            statsState.monthlyCapsules.forEach { capsule ->
                addMonthlyCapsule(document, capsule)
            }
        }
    }

    private fun addMonthlyCapsule(document: Document, capsule: MonthlySoundCapsule) {
        val monthHeader = Paragraph(capsule.month, FONT_SUBTITLE)
        monthHeader.alignment = Element.ALIGN_CENTER
        monthHeader.spacingBefore = 25f
        monthHeader.spacingAfter = 15f
        document.add(monthHeader)

        val divider = Paragraph("----------------------------------------", FONT_SMALL)
        divider.alignment = Element.ALIGN_CENTER
        divider.spacingAfter = 15f
        document.add(divider)

        document.add(Paragraph("Total Listening Time: ${formatTime(capsule.totalTimeListened)}", FONT_NORMAL))
        document.add(Paragraph("Songs Listened: ${capsule.totalSongsListened}", FONT_NORMAL))
        document.add(Paragraph("Artists Discovered: ${capsule.totalArtistsListened}", FONT_NORMAL))
        
        // Add daily average listening time
        val dailyAverage = capsule.totalTimeListened / 30 // Approximating a month as 30 days
        document.add(Paragraph("Daily Average Listening Time: ${formatTime(dailyAverage)}", FONT_NORMAL))

        
        document.add(Paragraph("\n", FONT_SMALL))

        capsule.topSong?.let { song ->
            document.add(Paragraph("Top Song: ${song.title} by ${song.artist}", FONT_SUBTITLE))
            document.add(Paragraph("Listened for: ${formatTime(song.timeListened)}", FONT_NORMAL))
            document.add(Paragraph("\n", FONT_SMALL))
        }

        capsule.topArtist?.let { artist ->
            document.add(Paragraph("Top Artist: ${artist.artist}", FONT_SUBTITLE))
            document.add(Paragraph("Listened for: ${formatTime(artist.totalTime)}", FONT_NORMAL))
            document.add(Paragraph("\n", FONT_SMALL))
        }

        // Top songs list
        if (capsule.topSongs.isNotEmpty()) {
            document.add(Paragraph("All Top Songs:", FONT_SUBTITLE))
            document.add(Paragraph("\n", FONT_SMALL))
            val songsTable = PdfPTable(3)
            songsTable.widthPercentage = 100f
            try {
                songsTable.setWidths(floatArrayOf(1.5f, 1f, 0.8f))
            } catch (e: DocumentException) {
                Log.e(TAG, "Error setting table widths", e)
            }

            // Add headers
            addCell(songsTable, "Song", true)
            addCell(songsTable, "Artist", true)
            addCell(songsTable, "Time", true)

            // Add rows
            capsule.topSongs.take(5).forEach { song ->
                addCell(songsTable, song.title)
                addCell(songsTable, song.artist)
                addCell(songsTable, formatTime(song.timeListened))
            }

            document.add(songsTable)
        }

        document.add(Paragraph("\n", FONT_SMALL))

        // Top artists list
        if (capsule.topArtists.isNotEmpty()) {
            document.add(Paragraph("All Top Artists:", FONT_SUBTITLE))
            document.add(Paragraph("\n", FONT_SMALL))
            val artistsTable = PdfPTable(2)
            artistsTable.widthPercentage = 100f
            try {
                artistsTable.setWidths(floatArrayOf(1.5f, 0.8f))
            } catch (e: DocumentException) {
                Log.e(TAG, "Error setting table widths", e)
            }

            // Add headers
            addCell(artistsTable, "Artist", true)
            addCell(artistsTable, "Time", true)

            // Add rows
            capsule.topArtists.take(5).forEach { artist ->
                addCell(artistsTable, artist.artist)
                addCell(artistsTable, formatTime(artist.totalTime))
            }

            document.add(artistsTable)
        }

        document.add(Paragraph("\n\n", FONT_SMALL))
    }

    private fun addCell(table: PdfPTable, text: String, isHeader: Boolean = false) {
        try {
            val cell = PdfPCell(Phrase(text, if (isHeader) FONT_SUBTITLE else FONT_NORMAL))
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.paddingTop = 5f
            cell.paddingBottom = 5f

            if (isHeader) {
                cell.backgroundColor = WHITE
            } else {
                cell.backgroundColor = WHITE
            }

            table.addCell(cell)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding cell to table: $text", e)
            table.addCell("")
        }
    }

    fun sharePdf(context: Context, fileUri: Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Purrytify Sound Capsule Report")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Sound Capsule via"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing PDF", e)
        }
    }
}