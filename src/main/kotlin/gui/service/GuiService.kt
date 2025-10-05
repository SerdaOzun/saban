package com.saban.gui.service

import com.saban.core.service.PronunciationService
import com.saban.core.service.RequestService
import com.saban.gui.model.PaginatedPronunciationResponse
import com.saban.gui.model.PronunciationResult
import com.saban.gui.model.SearchResult
import com.saban.gui.model.SettingsResponse
import com.saban.gui.model.requests.PaginatedPronunciationsRequest
import com.saban.gui.model.requests.PronunciationSaveRequest
import com.saban.gui.model.requests.PronunciationSearchRequest
import com.saban.storage.S3Service
import com.saban.user.repository.UserRepository
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
    private val userRepository: UserRepository by inject()
    private val requestService: RequestService by inject()

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
    suspend fun getPronunciations(pronunciationSearchRequest: PronunciationSearchRequest): List<PronunciationResult> {
        val (searchText, language) = pronunciationSearchRequest

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

    fun updateCountry(userId: Int, country: String) = userRepository.updateCountry(userId, country)
    fun getSettings(userId: Int): SettingsResponse = SettingsResponse(userRepository.getCountry(userId))
    fun saveRequest(userId: Int, request: PronunciationSaveRequest) = requestService.save(userId, request)
    fun getRequests(request: PaginatedPronunciationsRequest): PaginatedPronunciationResponse =
        requestService.get(request)

}