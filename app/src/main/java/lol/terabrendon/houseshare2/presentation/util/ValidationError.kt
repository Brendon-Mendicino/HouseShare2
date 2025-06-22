package lol.terabrendon.houseshare2.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.brendonmendicino.aformvalidator.annotation.ValidationError
import lol.terabrendon.houseshare2.R

@Composable
fun ValidationError.errorText(label: String): String = errorText(label, LocalContext.current)

fun ValidationError.errorText(label: String, context: Context) = when (this) {
    ValidationError.NotNull,
    is ValidationError.NotBlank -> context.getString(R.string.should_not_be_blank, label)

    is ValidationError.Pattern -> context.getString(
        R.string.does_not_match_the_correct_pattern,
        label
    )

    is ValidationError.Size -> context.getString(
        R.string.size_should_be_between_and,
        label,
        min.toString(),
        max.toString()
    )

    is ValidationError.Email -> context.getString(R.string.is_not_a_valid_email, label)
    is ValidationError.Max -> context.getString(
        R.string.should_not_be_greater_than,
        label,
        max.toString()
    )

    is ValidationError.MaxDouble -> context.getString(
        R.string.should_not_be_greater_than,
        label,
        max.toString()
    )

    is ValidationError.Min -> context.getString(
        R.string.should_not_be_less_than,
        label,
        min.toString()
    )

    is ValidationError.MinDouble -> context.getString(
        R.string.should_not_be_less_than,
        label,
        min.toString()
    )

    is ValidationError.ToNumber -> context.getString(R.string.is_not_a_valid_number, label)
}