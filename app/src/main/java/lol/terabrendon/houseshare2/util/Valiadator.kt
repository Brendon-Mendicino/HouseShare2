package lol.terabrendon.houseshare2.util

import io.github.brendonmendicino.aformvalidator.annotation.ValidationError
import io.github.brendonmendicino.aformvalidator.annotation.Validator
import io.github.brendonmendicino.aformvalidator.annotation.ValidatorCond

// TODO: move this classes to the library!!

class IsTrueValidator : ValidatorCond<Boolean?, ValidationError.Pattern> {
    override val conditions: List<(Boolean?) -> ValidationError.Pattern?>
        get() = listOf { if (it == true) null else ValidationError.Pattern("true") }
}

@Validator<ValidationError>(
    value = IsTrueValidator::class,
    errorType = ValidationError::class,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class IsTrue

class IsFalseValidator : ValidatorCond<Boolean?, ValidationError.Pattern> {
    override val conditions: List<(Boolean?) -> ValidationError.Pattern?>
        get() = listOf { if (it == false) null else ValidationError.Pattern("true") }
}

@Validator<ValidationError>(
    value = IsFalseValidator::class,
    errorType = ValidationError::class,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class IsFalse
