package lol.terabrendon.houseshare2.domain.kserializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URI

typealias URIAsStr = @Serializable(URIAsStrSerializer::class) URI

class URIAsStrSerializer : KSerializer<URI> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(URIAsStrSerializer::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URI) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): URI = URI.create(decoder.decodeString())
}