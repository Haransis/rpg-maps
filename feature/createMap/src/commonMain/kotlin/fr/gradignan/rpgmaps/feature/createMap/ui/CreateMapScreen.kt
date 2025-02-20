package fr.gradignan.rpgmaps.feature.createMap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateMapScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateMapViewModel = koinViewModel<CreateMapViewModel>()
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text("CreateMap Screen")
    }
}