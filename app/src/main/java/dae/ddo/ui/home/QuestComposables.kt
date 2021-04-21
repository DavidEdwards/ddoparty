package dae.ddo.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dae.ddo.R
import dae.ddo.entities.Quest
import dae.ddo.state.NetworkState
import dae.ddo.testdata.SampleQuestProvider
import dae.ddo.ui.support.viewModel
import dae.ddo.viewmodels.QuestViewModel

@Composable
fun QuestScreen() {
    val vm: QuestViewModel by viewModel()

    val searchTerm = remember { mutableStateOf("") }
    val state = vm.quests().collectAsState(initial = emptyList())
    val networkState = vm.networkState().collectAsState(initial = NetworkState.None)

    val quests = state.value
        .filter {
            it.name.contains(searchTerm.value, ignoreCase = true) ||
                    it.patron?.contains(searchTerm.value, ignoreCase = true) == true
        }

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .padding(8.dp)
                .alpha(0.9f),
            elevation = 4.dp
        ) {
            val focusManager = LocalFocusManager.current
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = searchTerm.value,
                singleLine = true,
                onValueChange = { term ->
                    searchTerm.value = term
                },
                label = {
                    Text(text = LocalContext.current.getString(R.string.search))
                },
                placeholder = {
                    Text(text = LocalContext.current.getString(R.string.type_quest_name))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = LocalContext.current.getString(R.string.search)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface,
                    placeholderColor = MaterialTheme.colors.onSurface.copy(0.8f),
                    focusedLabelColor = MaterialTheme.colors.onSurface.copy(0.8f),
                    unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(0.5f)
                ),
                keyboardActions = KeyboardActions {
                    focusManager.clearFocus()
                }
            )
        }

        if (networkState.value != NetworkState.Loading) {
            if (quests.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(
                            top = 72.dp,
                            bottom = 48.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                    text = LocalContext.current.getString(R.string.no_quests),
                    textAlign = TextAlign.Center
                )
            } else {
                QuestList(quests = quests)
            }
        } else {
            LoadingIndicator()
        }
    }
}

@Preview
@Composable
fun QuestList(@PreviewParameter(SampleQuestProvider::class, 1) quests: List<Quest>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentPadding = PaddingValues(
            top = 72.dp,
            bottom = 48.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {
        items(quests) { quest ->
            QuestCard(quest = quest)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestCard(quest: Quest) {
    var optionsShown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                optionsShown = !optionsShown
            },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            QuestTitle(text = quest.name)

            quest.patron?.let { patron ->
                Text(text = patron)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = LocalContext.current.getString(
                            R.string.heroic_cr_x,
                            quest.heroicCR
                        )
                    )
                    quest.epicCR?.let { cr ->
                        Text(text = LocalContext.current.getString(R.string.epic_cr_x, cr))
                    }
                }
            }

            CardControls(optionsShown = optionsShown, quest = quest)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}