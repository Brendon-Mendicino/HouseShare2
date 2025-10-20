package lol.terabrendon.houseshare2.domain.typeadapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.OffsetDateTime

class OffsetDateTimeSerde : JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): OffsetDateTime? {
        if (json == null || json.isJsonNull) return null
        return try {
            OffsetDateTime.parse(json.asString)
        } catch (e: Exception) {
            throw JsonParseException(
                "Invalid OffsetDateTime format: ${json.asString}",
                e
            )
        }
    }

    override fun serialize(
        src: OffsetDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src == null) return JsonNull.INSTANCE
        return JsonPrimitive(src.toString()) // ISO-8601 format
    }
}
