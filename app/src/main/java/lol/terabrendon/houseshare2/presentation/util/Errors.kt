package lol.terabrendon.houseshare2.presentation.util

import androidx.annotation.StringRes
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Email
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Max
import io.github.brendonmendicino.aformvalidator.annotation.annotations.MaxDouble
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Min
import io.github.brendonmendicino.aformvalidator.annotation.annotations.MinDouble
import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotBlank
import io.github.brendonmendicino.aformvalidator.annotation.annotations.NotNull
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Pattern
import io.github.brendonmendicino.aformvalidator.annotation.annotations.Size
import io.github.brendonmendicino.aformvalidator.annotation.annotations.ToNumber
import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.error.FormError
import lol.terabrendon.houseshare2.domain.error.LocalError
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.domain.error.RootError
import lol.terabrendon.houseshare2.util.IsTrue
import lol.terabrendon.houseshare2.util.Url

fun RootError.toUiText(): UiText = when (this) {
    is DataError -> toUiText()
    is FormError -> toUiText()
}

fun DataError.toUiText(): UiText = when (this) {
    is LocalError -> toUiText()
    is RemoteError -> toUiText()
}

fun LocalError.toUiText(): UiText = when (this) {
    is LocalError.Constraint -> TODO()
    is LocalError.OutOfMemory -> UiText.Res(R.string.the_device_is_out_of_memory)
    is LocalError.Unknown -> UiText.Res(R.string.an_unknown_error_happened_while_accessing_the_file_system)
}

fun RemoteError.toUiText(): UiText {
    val msg = when (this) {
        is RemoteError.BadRequest -> UiText.Res(R.string.the_server_could_not_handle_the_request)
        is RemoteError.BadGateway -> UiText.Res(R.string.gateway_got_invalid_repsonse_while_processing_the_request)
        is RemoteError.ContentTooLarge -> UiText.Res(R.string.the_uploaded_content_was_too_large)
        is RemoteError.Unauthorized -> UiText.Res(R.string.session_expired_please_sign_in_again)
        is RemoteError.Forbidden -> UiText.Res(R.string.you_are_not_authorized_to_make_such_request)
        is RemoteError.Redirect -> UiText.Res(R.string.the_resource_has_changed_location)
        is RemoteError.GatewayTimeout -> UiText.Res(R.string.gateway_timed_out)
        is RemoteError.NoConnection -> UiText.Res(R.string.no_internet_connection)
        is RemoteError.NoSession -> UiText.Res(R.string.session_expired_please_sign_in_again)
        is RemoteError.InvalidCredentials -> reason
            ?.let { UiText.Dyn(it) }
            ?: UiText.Res(R.string.wrong_username_or_password)
        is RemoteError.NotFound -> UiText.Res(R.string.the_request_resource_does_not_exist)
        is RemoteError.RequestTimeout -> UiText.Res(R.string.the_request_took_too_long_to_elaborate)
        is RemoteError.ServiceUnavailable -> UiText.Res(R.string.the_server_is_temporarily_unavailable)
        is RemoteError.TooManyRequests -> UiText.Res(R.string.the_server_is_too_busy_to_answer_at_the_moment)
        is RemoteError.UnsupportedMediaType -> UiText.Res(R.string.the_file_format_you_uploaded_is_not_processable_by_the_server)
        is RemoteError.InternalServerError -> UiText.Res(R.string.the_server_experienced_an_unrecoverable_error_while_processing_the_request)

        is RemoteError.Unknown -> UiText.Res(R.string.an_unknown_network_error_happened)
    }

    return UiText.Multi(
        msg,
        "\n".toUiText(),
        UiText.Res(R.string.network_error),
        ": ${message()}".toUiText()
    )
}

fun FormError.toUiText(): UiText = when (this) {
    is FormError.Validation -> error.toUiText(label)
}

fun ValidationError<*>.toUiText(@StringRes label: Int?): UiText =
    annotation.toUiText(label?.let { UiText.Res(it) })

fun ValidationError<*>.toUiText(label: UiText?): UiText = annotation.toUiText(label)

/**
 * The message of a failed validation is built from the annotation that failed, and not from the
 * [ValidationError] subtype: custom annotations like [Url] and [IsTrue] can only reuse
 * [ValidationError.PatternErr], so switching on the error would make every one of them read
 * "does not match the correct pattern".
 */
private fun Annotation.toUiText(label: UiText?): UiText {
    if (label == null) return UiText.Res(R.string.this_value_is_not_valid)

    return when (this) {
        is NotBlank,
        is NotNull,
            -> UiText.Res(R.string.should_not_be_blank, label)

        is Size -> UiText.Res(
            R.string.size_should_be_between_and,
            label,
            min.toString(),
            max.toString(),
        )

        is Pattern -> UiText.Res(R.string.does_not_match_the_correct_pattern, label)
        is Email -> UiText.Res(R.string.is_not_a_valid_email, label)
        is Min -> UiText.Res(R.string.should_not_be_less_than, label, min.toString())
        is MinDouble -> UiText.Res(R.string.should_not_be_less_than, label, min.toString())
        is Max -> UiText.Res(R.string.should_not_be_greater_than, label, max.toString())
        is MaxDouble -> UiText.Res(R.string.should_not_be_greater_than, label, max.toString())
        is ToNumber -> UiText.Res(R.string.is_not_a_valid_number, label)
        is Url -> UiText.Res(R.string.is_not_a_valid_link, label)

        // Annotation is not a sealed type, so a fallback is needed. It also covers the
        // annotations that only state that something is wrong, like IsTrue and IsFalse.
        else -> UiText.Res(R.string.is_not_valid, label)
    }
}

fun String.toUiText() = UiText.Dyn(this)
