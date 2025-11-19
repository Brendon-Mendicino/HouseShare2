package lol.terabrendon.houseshare2.util

import io.github.brendonmendicino.aformvalidator.annotation.annotations.Pattern
import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError
import io.github.brendonmendicino.aformvalidator.core.Metadata
import io.github.brendonmendicino.aformvalidator.core.Validator
import io.github.brendonmendicino.aformvalidator.core.ValidatorCond
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
