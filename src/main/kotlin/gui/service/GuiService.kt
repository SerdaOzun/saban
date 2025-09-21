package com.saban.gui.service

import com.saban.core.service.PronunciationService
import com.saban.gui.model.PronunciationResult
import com.saban.gui.model.SearchResult
import com.saban.gui.model.requests.PronunciationRequest
import com.saban.storage.S3Service
import com.saban.util.S3UploadException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class GuiService : KoinComponent {
    private val pronunciationService: PronunciationService by inject()

    private val s3Service: S3Service by inject()

    suspend fun extractFileAndFilename(multipart: MultiPartData, word: String, username: String): Pair<String, File> {
        lateinit var fileName: String
        lateinit var audioFile: File

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    fileName = "${word}_${username}_${System.currentTimeMillis()}.webm"
                    val file = File("uploads/$fileName").apply {
                        parentFile?.mkdirs()
                    }
                    part.provider().copyAndClose(file.writeChannel())
                    audioFile = file
                }

                else -> {}
            }
            part.dispose()
        }

        return Pair(fileName, audioFile)
    }

    /**
     * Find matches across all languages for the search term
     */
    fun findMatches(searchText: String): Map<String, List<SearchResult>> =
        pronunciationService.findMatches(searchText)

    /**
     * Get pronunciations for search term and language
     */
    suspend fun getPronunciations(pronunciationRequest: PronunciationRequest): List<PronunciationResult> {
        val (searchText, language) = pronunciationRequest

        return pronunciationService.getPronunciations(searchText, language).map {
            it.copy(
                url = s3Service.getPresignedUrl(it.s3key, language),
                s3key = ""
            )
        }
    }

    suspend fun savePronunciation(
        audioFile: File,
        objectName: String,
        word: String,
        userId: Int,
        language: String
    ) = coroutineScope {
        val fileKey = runCatching { s3Service.savePronunciation(audioFile, objectName, language) }.getOrNull()
            ?: throw S3UploadException("Failed to upload pronunciation to S3 bucket")
        pronunciationService.savePronunciation(userId, word, language, fileKey)
    }


}