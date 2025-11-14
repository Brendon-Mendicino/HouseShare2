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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChooseGroup(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No group selected", style = MaterialTheme.typography.displaySmallEmphasized)
            Text("No group selected", style = MaterialTheme.typography.displaySmall)

            Spacer(Modifier.height(16.dp))

            Text("Please go to the group tab and select a group.", fontStyle = FontStyle.Italic)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChoosePreview() {
    HouseShare2Theme {
        ChooseGroup(modifier = Modifier.fillMaxSize())
    }
}