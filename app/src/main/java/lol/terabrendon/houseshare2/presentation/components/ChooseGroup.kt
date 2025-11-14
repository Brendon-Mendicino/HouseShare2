package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lol.terabrendon.houseshare2.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChooseGroup(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.no_group_selected),
                style = MaterialTheme.typography.displaySmallEmphasized
            )

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.please_go_to_the_group_tab_and_select_a_group))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChoosePreview() {
    ChooseGroup(modifier = Modifier.fillMaxSize())
}