package lol.terabrendon.houseshare2.util

import io.github.brendonmendicino.aformvalidator.annotation.annotations.Pattern
import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError
import io.github.brendonmendicino.aformvalidator.core.Metadata
import io.github.brendonmendicino.aformvalidator.core.Validator
import io.github.brendonmendicino.aformvalidator.core.ValidatorCond
import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

// TODO: move this classes to the library!!

class IsTrueValidator(
    override val metadata: Metadata?,
    override val annotation: IsTrue,
) : ValidatorCond<Boolean?, IsTrue, ValidationError.PatternErr>(metadata, annotation) {
    override fun isValid(value: Boolean?): ValidationError.PatternErr? {
        return if (value == true) null
        else ValidationError.PatternErr(metadata, Pattern())
    }
}

@Validator(IsTrueValidator::class)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class IsTrue(
    val metadata: KClass<out Metadata> = Nothing::class,
)

class IsFalseValidator(
    override val metadata: Metadata?,
    override val annotation: IsFalse,
) : ValidatorCond<Boolean?, IsFalse, ValidationError.PatternErr>(metadata, annotation) {
    override fun isValid(value: Boolean?): ValidationError.PatternErr? {
        return if (value == false) null
        else ValidationError.PatternErr(metadata, Pattern())
    }
}

@Validator(IsFalseValidator::class)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class IsFalse(
    val metadata: KClass<out Metadata> = Nothing::class,
)

/**
 * Validates a [CharSequence] as a well-formed HTTP(S) URL by actually parsing it with
 * [java.net.URI], instead of matching it against a handwritten regex. `null` is
 * considered valid.
 */
class UrlValidator(
    override val metadata: Metadata?,
    override val annotation: Url,
) : ValidatorCond<CharSequence?, Url, ValidationError.PatternErr>(metadata, annotation) {
    override fun isValid(value: CharSequence?): ValidationError.PatternErr? {
        if (value == null) return null

        val isValidUrl = try {
            val uri = URI(value.toString())
            (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
        } catch (_: URISyntaxException) {
            false
        }

        return if (isValidUrl) null
        else ValidationError.PatternErr(metadata, Pattern())
    }
}

@Validator(UrlValidator::class)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class Url(
    val metadata: KClass<out Metadata> = Nothing::class,
)
