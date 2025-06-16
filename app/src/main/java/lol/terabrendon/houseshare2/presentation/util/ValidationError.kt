package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.brendonmendicino.aformvalidator.annotation.ValidationError
import lol.terabrendon.houseshare2.R

@Composable
fun ValidationError.errorText(label: String): String = when (this) {
    ValidationError.NotBlank -> stringResource(R.string.should_not_be_blank, label)
    ValidationError.Pattern -> stringResource(R.string.does_not_match_the_correct_pattern, label)
    ValidationError.Size -> stringResource(R.string.does_not_match_the_correct_size, label)
}