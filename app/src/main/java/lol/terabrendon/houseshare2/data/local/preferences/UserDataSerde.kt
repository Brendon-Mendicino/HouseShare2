package lol.terabrendon.houseshare2.data.local.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserDataSerializer : Serializer<UserData> {
    override val defaultValue: UserData
        get() = UserData()

    override suspend fun readFrom(input: InputStream): UserData {
        return try {
            Json.decodeFromString<UserData>(input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read UserData", e)
        }
    }

    override suspend fun writeTo(
        t: UserData,
        output: OutputStream,
    ) {
        output.write(Json.encodeToString(t).encodeToByteArray())
    }
}