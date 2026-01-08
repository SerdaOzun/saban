package com.saban.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true

            serializersModule = serializersModuleOf(OffsetDateTimeSerializer)
        })
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = OffsetDateTime::class)
object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString(), formatter)
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.format(formatter))
    }
}