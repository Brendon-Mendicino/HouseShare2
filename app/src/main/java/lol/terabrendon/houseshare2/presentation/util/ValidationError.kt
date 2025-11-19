package lol.terabrendon.houseshare2.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.brendonmendicino.aformvalidator.annotation.error.ValidationError
import lol.terabrendon.houseshare2.R

@Composable
fun ValidationError<*>.errorText(label: String): String = errorText(label, LocalContext.current)

fun ValidationError<*>.errorText(label: String, context: Context) = when (this) {
    is ValidationError.NotNullErr,
    is ValidationError.NotBlankErr,
        -> context.getString(R.string.should_not_be_blank, label)

    is ValidationError.PatternErr -> context.getString(
        R.string.does_not_match_the_correct_pattern,
        label
    )

    is ValidationError.SizeErr -> context.getString(
        R.string.size_should_be_between_and,
        label,
        this.annotation.min.toString(),
        this.annotation.max.toString()
    )

    is ValidationError.EmailErr -> context.getString(R.string.is_not_a_valid_email, label)
    is ValidationError.MaxErr -> context.getString(
        R.string.should_not_be_greater_than,
        label,
        this.annotation.max.toString()
    )

    is ValidationError.MaxDoubleErr -> context.getString(
        R.string.should_not_be_greater_than,
        label,
        this.annotation.max.toString()
    )

    is ValidationError.MinErr -> context.getString(
        R.string.should_not_be_less_than,
        label,
        this.annotation.min.toString()
    )

    is ValidationError.MinDoubleErr -> context.getString(
        R.string.should_not_be_less_than,
        label,
        this.annotation.min.toString()
    )

    is ValidationError.ToNumberErr -> context.getString(R.string.is_not_a_valid_number, label)
}