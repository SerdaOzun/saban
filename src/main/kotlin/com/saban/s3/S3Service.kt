package com.saban.s3

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.saban.languages.LanguageService
import com.saban.plugins.S3Config
import com.saban.plugins.SabanConfig
import org.koin.core.component.KoinComponent
import java.io.File
import java.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class S3Service(
    config: SabanConfig,
    private val languageService: LanguageService
) : KoinComponent {

    private val client = buildS3Client(config.s3)

    private val presignedUrlsCache: Cache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofHours(2))
        .build()

    suspend fun listBucketNames(): List<String>? {
        return client.listBuckets().buckets?.mapNotNull { it.name } ?: emptyList()
    }

    /**
     * Create buckets for languages that don't have one yet
     */
    suspend fun createLanguageBuckets() {
        languageService.getLanguages()
            .filter { lang ->
                runCatching {
                    client.headBucket(HeadBucketRequest.Companion { bucket = lang.name })
                }.isFailure
            }
            .forEach { lang ->
                client.createBucket(
                    CreateBucketRequest.Companion {
                        bucket = lang.name
                    }
                )
            }
    }

    /**
     * @return key of uploaded pronunciation
     */
    suspend fun savePronunciation(
        audioFile: File,
        objectKey: String,
        language: String,
    ): String {
        client.putObject(
            PutObjectRequest.Companion {
                bucket = language
                key = objectKey
                body = audioFile.asByteStream()
                acl = ObjectCannedAcl.PublicRead
            }
        )

        return objectKey
    }

    suspend fun getPresignedUrl(objectKey: String, language: String): String {
        return presignedUrlsCache.getIfPresent(objectKey) ?: generatePresignedUrl(language, objectKey)
            .also { presignedUrlsCache.put(objectKey, it) }
    }

    private suspend fun generatePresignedUrl(
        bucketName: String,
        objectKey: String
    ): String {
        val unsignedRequest = GetObjectRequest {
            bucket = bucketName
            key = objectKey
        }

        val presignedRequest = client.presignGetObject(unsignedRequest, 2.hours + 30.seconds)

        return presignedRequest.url.toString()
    }

    private fun buildS3Client(config: S3Config): S3Client {
        val credentials = StaticCredentialsProvider.Companion {
            accessKeyId = config.keyId
            secretAccessKey = config.secret
        }

        return S3Client.Companion {
            region = "us-east-1"
            endpointUrl = Url.Companion.parse("${config.protocol}://${config.endpoint}")
            forcePathStyle = true
            credentialsProvider = credentials
        }
    }
}