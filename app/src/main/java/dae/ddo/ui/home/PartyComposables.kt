package dae.ddo.ui.home

import android.graphics.Color.TRANSPARENT
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import dae.ddo.R
import dae.ddo.entities.Player
import dae.ddo.state.NetworkState
import dae.ddo.testdata.SamplePartyUiDataProvider
import dae.ddo.ui.data.PartyUiData
import dae.ddo.ui.support.viewModel
import dae.ddo.utils.extensions.orElse
import dae.ddo.viewmodels.PartyViewModel
import java.util.*

@Composable
fun PartyScreen() {
    val vm: PartyViewModel by viewModel()

    val partyGroupState = vm.parties().collectAsState(initial = emptyList())
    val filterState = vm.filtersAndConditions().collectAsState(initial = emptyList())
    val networkState = vm.networkState().collectAsState(initial = NetworkState.None)

    val partyServerGroups = partyGroupState.value
    if (partyServerGroups.isEmpty() && networkState.value is NetworkState.Loading) {
        LoadingIndicator()
    } else if (networkState.value is NetworkState.NotAvailable) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = (networkState.value as NetworkState.NotAvailable).text,
            color = MaterialTheme.colors.error
        )
    } else {
        if (partyServerGroups.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        bottom = 48.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                text = LocalContext.current.getString(R.string.no_parties),
                textAlign = TextAlign.Center
            )
        } else {
            val partyUiDataList = vm.sortRelevancy(partyServerGroups, filterState.value)
            val dataList = partyUiDataList
                .sortedByDescending { it.relevancy }

            PartyList(dataList = dataList)
        }
    }
}

@Preview
@Composable
fun PartyList(
    @PreviewParameter(SamplePartyUiDataProvider::class, 1) dataList: List<PartyUiData>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 48.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {
        items(dataList) { partyUiData ->
            PartyCard(partyUiData = partyUiData)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PartyCard(partyUiData: PartyUiData) {
    val party = partyUiData.party

    var optionsShown by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = "${party.id}", block = {
        optionsShown = false
    })

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
            PartyCardHeader(partyUiData = partyUiData)

            Spacer(modifier = Modifier.height(8.dp))

            val allPlayers = (listOf(party.leader) + party.members.orEmpty())
            QuestPlayerContainer(allPlayers)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                ChipList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    partyUiData = partyUiData
                )
            }

            CardControls(optionsShown = optionsShown, party = party)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun PartyCardHeader(partyUiData: PartyUiData) {
    val party = partyUiData.party

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f, fill = true)
        ) {
            party.quest?.let { quest ->
                QuestTitle(text = quest.name.orElse("No name"))
                if (quest.name != quest.adventureArea) {
                    QuestSubtitle(text = quest.adventureArea.orElse("No area"))
                    if (quest.groupSize == "Raid") {
                        Text(
                            text = LocalContext.current.getString(R.string.raid),
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.secondaryVariant
                        )
                    }
                }

                if (!party.comment.isNullOrBlank()) {
                    Text(
                        text = party.comment.orElse(LocalContext.current.getString(R.string.no_comment)),
                        style = MaterialTheme.typography.body2
                    )
                }
            } ?: run {
                if (party.comment.isNullOrBlank()) {
                    if (party.leader.location.name.isBlank()) {
                        QuestTitle(text = LocalContext.current.getString(R.string.no_comment))
                    } else {
                        QuestTitle(text = party.leader.location.name)
                    }
                } else {
                    QuestTitle(
                        text = party.comment.orElse(
                            LocalContext.current.getString(
                                R.string.no_comment
                            )
                        )
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            QuestRelevancy(
                relevancy = partyUiData.relevancy,
                maxRelevancy = partyUiData.maxRelevancy
            )
        }
    }

    Text(text = "Server: ${party.server}")
}

@Composable
fun QuestTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
fun QuestSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Light
    )
}

@Composable
fun QuestPlayerContainer(list: List<Player>) {
    QuestPlayerCount(count = list.size)
    QuestPlayerList(list = list)
}

@Composable
fun QuestPlayerCount(count: Int) {
    Text(
        text = LocalContext.current.resources.getQuantityString(
            R.plurals.x_players,
            count,
            count
        ),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun QuestPlayerList(list: List<Player>) {
    list.forEach { player ->
        val totalLevel = player.classes.sumBy { clazz -> clazz.level }
        val classesList =
            player.classes.map { clazz -> "${clazz.level} ${clazz.name.substring(0, 3)}" }
                .joinToString(", ")

        val levelText = if (player.classes.size > 1) {
            " ($totalLevel)"
        } else {
            ""
        }
        val classesText = if (player.classes.isNotEmpty()) {
            " ($classesList)"
        } else {
            ""
        }

        Text(
            text = "· ${player.name}$levelText$classesText"
        )
    }
}

@Composable
fun QuestRelevancy(relevancy: Int, maxRelevancy: Int) {
    val color = ColorUtils.blendARGB(
        TRANSPARENT,
        MaterialTheme.colors.secondary.toArgb(),
        relevancy.toFloat().div(maxRelevancy.toFloat())
    )

    Image(
        imageVector = Icons.Default.Star,
        contentDescription = LocalContext.current.getString(R.string.relevancy),
        colorFilter = ColorFilter.tint(Color(color), BlendMode.SrcIn)
    )
}

@Composable
fun ChipList(
    modifier: Modifier = Modifier,
    partyUiData: PartyUiData
) {
    val party = partyUiData.party

    if (partyUiData.matchedFilters.isNotEmpty() || party.difficulty != null) {
        val filterScrollState = rememberScrollState()
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = modifier.horizontalScroll(filterScrollState)
        ) {
            if (party.difficulty != null) {
                Chip(
                    text = party.difficulty.capitalize(Locale.getDefault()),
                    color = MaterialTheme.colors.primaryVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            partyUiData.matchedFilters.forEach { fc ->
                Chip(text = fc.filter.name)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun Chip(
    text: String,
    color: Color = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colors.onSecondary
) {
    Surface(
        color = color,
        contentColor = contentColor,
        shape = RoundedCornerShape(25),
        elevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = contentColor.copy(alpha = 0.5f)
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            text = text,
            style = MaterialTheme.typography.caption
        )
    }
}