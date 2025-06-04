package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.presentation.vm.GroupsViewModel

@Composable
fun GroupsScreen(viewModel: GroupsViewModel = hiltViewModel()) {
    val groups by viewModel.groups.collectAsState()

    GroupsScreenInner(groups = groups)
}

@Composable
fun GroupsScreenInner(groups: List<GroupInfoModel>) {
    Text(text = groups.toString())
}