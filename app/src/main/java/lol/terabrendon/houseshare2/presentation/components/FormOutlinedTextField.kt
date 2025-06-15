package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import io.github.brendonmendicino.aformvalidator.annotation.ParamState

/**
 * <a href="https://m3.material.io/components/text-fields/overview" class="external" target="_blank">Material Design outlined text field</a>.
 *
 * Text fields allow users to enter text into a UI. They typically appear in forms and dialogs.
 * Outlined text fields have less visual emphasis than filled text fields. When they appear in
 * places like forms, where many text fields are placed together, their reduced emphasis helps
 * simplify the layout.
 *
 * ![Outlined text field image](https://developer.android.com/images/reference/androidx/compose/material3/outlined-text-field.png)
 *
 * See example usage:
 * @sample androidx.compose.material3.samples.SimpleOutlinedTextFieldSample
 *
 * If apart from input text change you also want to observe the cursor location, selection range,
 * or IME composition use the OutlinedTextField overload with the [TextFieldValue] parameter
 * instead.
 *
 * @param value the input text to be shown in the text field
 * @param onValueChange the callback that is triggered when the input service updates the text. An
 * updated text comes as a parameter of the callback
 * @param modifier the [Modifier] to be applied to this text field
 * @param enabled controls the enabled state of this text field. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param readOnly controls the editable state of the text field. When `true`, the text field cannot
 * be modified. However, a user can focus it and copy text from it. Read-only text fields are
 * usually used to display pre-filled forms that a user cannot edit.
 * @param textStyle the style to be applied to the input text. Defaults to [LocalTextStyle].
 * @param label the optional label to be displayed inside the text field container. The default
 * text style for internal [Text] is [Typography.bodySmall] when the text field is in focus and
 * [Typography.bodyLarge] when the text field is not in focus
 * @param placeholder the optional placeholder to be displayed when the text field is in focus and
 * the input text is empty. The default text style for internal [Text] is [Typography.bodyLarge]
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the text field
 * container
 * @param trailingIcon the optional trailing icon to be displayed at the end of the text field
 * container
 * @param prefix the optional prefix to be displayed before the input text in the text field
 * @param suffix the optional suffix to be displayed after the input text in the text field
 * @param supportingText the optional supporting text to be displayed below the text field
 * @param isError indicates if the text field's current value is in error. If set to true, the
 * label, bottom indicator and trailing icon by default will be displayed in error color
 * @param visualTransformation transforms the visual representation of the input [value]
 * For example, you can use
 * [PasswordVisualTransformation][androidx.compose.ui.text.input.PasswordVisualTransformation] to
 * create a password text field. By default, no visual transformation is applied.
 * @param keyboardOptions software keyboard options that contains configuration such as
 * [KeyboardType] and [ImeAction]
 * @param keyboardActions when the input service emits an IME action, the corresponding callback
 * is called. Note that this IME action may be different from what you specified in
 * [KeyboardOptions.imeAction]
 * @param singleLine when `true`, this text field becomes a single horizontally scrolling text field
 * instead of wrapping onto multiple lines. The keyboard will be informed to not show the return key
 * as the [ImeAction]. Note that [maxLines] parameter will be ignored as the maxLines attribute will
 * be automatically set to 1.
 * @param maxLines the maximum height in terms of maximum number of visible lines. It is required
 * that 1 <= [minLines] <= [maxLines]. This parameter is ignored when [singleLine] is true.
 * @param minLines the minimum height in terms of minimum number of visible lines. It is required
 * that 1 <= [minLines] <= [maxLines]. This parameter is ignored when [singleLine] is true.
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this text field. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this text field in different states.
 * @param shape defines the shape of this text field's border
 * @param colors [TextFieldColors] that will be used to resolve the colors used for this text field
 * in different states. See [OutlinedTextFieldDefaults.colors].
 */
@Composable
fun <T : Any?, E : Any> FormOutlinedTextField(
    modifier: Modifier = Modifier,
    param: ParamState<T, E>,
    onValueChange: (String) -> Unit,
    value: String = param.value?.toString() ?: "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    errorConverter: (E) -> String = { it.toString() },
    supportingText: @Composable (() -> Unit)? = {
        param.error?.let {
            if (param.used) {
                Text(errorConverter(it))
            }
        }
    },
    isError: Boolean = param.isError,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}