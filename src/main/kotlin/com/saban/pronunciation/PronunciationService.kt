package com.saban.pronunciation

import com.saban.languages.LanguageService
import com.saban.pronunciation.model.PronunciationResult
import com.saban.request.RequestEntity
import com.saban.request.RequestService
import com.saban.search.SearchResult
import com.saban.s3.S3Service
import com.saban.util.MissingLanguageException
import com.saban.util.S3UploadException
import io.ktor.http.content.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import java.io.File

class PronunciationService(
    private val pronunciationRepo: PronunciationRepository,
    private val languageService: LanguageService,
    private val requestService: RequestService,
    private val s3Service: S3Service
) : KoinComponent {

    suspend fun getPronunciations(word: String, language: String): List<PronunciationResult> {
        return pronunciationRepo.getPronunciations(word, language).map {
            it.copy(
                url = s3Service.getPresignedUrl(it.s3key, language),
                s3key = ""
            )
        }
    }

    fun findMatches(searchText: String): Map<String, List<SearchResult>> =
        pronunciationRepo.searchEntriesByLanguage(searchText)

    /**
     * @return pronunciation id
     */
    suspend fun savePronunciation(
        audioFile: File,
        objectName: String,
        word: String,
        userId: Int,
        language: String
    ) = coroutineScope {
        val fileKey = runCatching { s3Service.savePronunciation(audioFile, objectName, language) }.getOrNull()
            ?: throw S3UploadException("Failed to upload pronunciation to S3 bucket")
        val langId = languageService.findLanguage(language)?.id
            ?: throw MissingLanguageException("Language '$language' not found")
        pronunciationRepo.savePronunciation(userId, word, langId, fileKey)
    }

    fun getRequest(requestId: Int): RequestEntity? = requestService.get(requestId)

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
}
