package dae.ddo.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dae.ddo.R
import dae.ddo.entities.*
import dae.ddo.state.LocatorEditCondition
import dae.ddo.state.LocatorEditFilter
import dae.ddo.state.LocatorTabFilters
import dae.ddo.state.NavigationLocator
import dae.ddo.ui.support.viewModel
import dae.ddo.viewmodels.FilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
private fun Int.relevancyString(): String {
    return when (this) {
        1 -> LocalContext.current.getString(R.string.relevancy_lowest)
        2 -> LocalContext.current.getString(R.string.relevancy_low)
        3 -> LocalContext.current.getString(R.string.relevancy_modest)
        4 -> LocalContext.current.getString(R.string.relevancy_high)
        5 -> LocalContext.current.getString(R.string.relevancy_extreme)
        else -> LocalContext.current.getString(R.string.relevancy_flag)
    }
}

@Composable
fun FilterListScreen(
    activeTab: MutableState<NavigationLocator>
) {
    val vm: FilterViewModel by viewModel()

    val filterState = vm.filtersAndConditions().collectAsState(initial = emptyList())

    val filterConditions = filterState.value
    if (filterConditions.isNotEmpty()) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            filterConditions.forEach { filterCondition ->
                FilterCard(activeTab = activeTab, filterConditions = filterCondition)
            }

            Spacer(modifier = Modifier.height(144.dp))
        }
    } else {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = LocalContext.current.getString(R.string.no_filters),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FilterCard(
    activeTab: MutableState<NavigationLocator>,
    filterConditions: FilterConditions
) {
    val vm: FilterViewModel by viewModel()
    val scope = rememberCoroutineScope()

    val filter = filterConditions.filter
    val conditions = filterConditions.conditions

    val enabledState = remember { mutableStateOf(true) }

    LaunchedEffect(key1 = "filterCardLoad,${filter.id}") {
        enabledState.value = filter.enabled
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                activeTab.value = LocatorEditFilter(filterId = filter.id)
            },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FilterTitle(text = filter.name, mustMatch = filter.mustMatch)
            if(filter.mustMatch) {
                Text(text = LocalContext.current.getString(R.string.must_match))
            } else {
                Text(text = filter.relevancy.relevancyString())
            }
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    conditions.forEachIndexed { index, condition ->
                        Text(
                            text = "${index + 1}. ${condition.type.name} — ${
                                when (condition.type) {
                                    ConditionType.NONE -> "?"
                                    ConditionType.SERVER -> condition.arg1
                                    ConditionType.GUILD -> condition.arg1
                                    ConditionType.PLAYER -> condition.arg1
                                    ConditionType.TEXT -> condition.arg1
                                    ConditionType.LEVEL -> "${condition.arg2} - ${condition.arg3}"
                                    ConditionType.QUEST -> condition.arg1
                                    ConditionType.RAID -> condition.arg5
                                    ConditionType.DIFFICULTY -> condition.arg1
                                }
                            }"
                        )
                    }
                }
                Switch(checked = enabledState.value, onCheckedChange = { enabled ->
                    enabledState.value = enabled

                    val saveFilter = FilterConditions(
                        filter = filter.copy(enabled = enabled),
                        conditions = conditions
                    )

                    scope.launch {
                        vm.saveFilter(saveFilter)
                    }
                })
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun FilterTitle(
    text: String,
    mustMatch: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.ExtraBold,
        color = if (mustMatch) MaterialTheme.colors.secondary else Color.Unspecified
    )
}

@Composable
fun EditFilterScreen(
    activeTab: MutableState<NavigationLocator>,
    filterId: Long? = null
) {
    BackHandler(onBack = {
        activeTab.value = LocatorTabFilters
    })

    val vm: FilterViewModel by viewModel()
    val scope = rememberCoroutineScope()

    val canAddConditionState = remember { mutableStateOf(false) }
    val idState = remember { mutableStateOf(0L) }
    val nameState = remember { mutableStateOf("") }
    val mustMatchState = remember { mutableStateOf(false) }
    val conditionsState = remember { mutableStateOf(listOf<Condition>()) }
    val relevancyState = remember { mutableStateOf(0) }

    fun save() {
        scope.launch {
            val saveFilter = FilterConditions(
                filter = Filter(
                    id = filterId ?: idState.value,
                    name = nameState.value,
                    mustMatch = mustMatchState.value,
                    relevancy = relevancyState.value,
                    enabled = true
                ),
                conditions = conditionsState.value
            )
            val id = vm.saveFilter(saveFilter)

            activeTab.value = LocatorEditFilter(
                id ?: idState.value
            )
        }
    }

    LaunchedEffect(key1 = "filterConditionLoad,$filterId") {
        val filterConditions = withContext(Dispatchers.IO) {
            filterId?.let { vm.getFilterConditions(filterId).firstOrNull() }
                ?: FilterConditions.dummy()
        }

        filterConditions.apply {
            idState.value = filter.id
            nameState.value = filter.name
            mustMatchState.value = filter.mustMatch
            conditionsState.value = conditions
            relevancyState.value = filter.relevancy

            canAddConditionState.value = filter.name.isNotBlank()
        }

        if(filterConditions.filter.id == 0L) {
            save()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = nameState.value,
            onValueChange = { name ->
                canAddConditionState.value = name.isNotBlank()
                nameState.value = name
                Timber.v("TEXTED")
            },
            label = {
                Text(text = LocalContext.current.getString(R.string.filter_name))
            },
            placeholder = {
                Text(text = LocalContext.current.getString(R.string.what_is_filter_called))
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.surface,
                textColor = MaterialTheme.colors.onSurface
            )
        )

        StandardVerticalSpacing()

        LabelledSwitch(
            text = LocalContext.current.getString(R.string.only_show_matching_parties),
            checked = mustMatchState
        )

        StandardVerticalSpacing()

        if (!mustMatchState.value) {
            RelevancySlider(
                state = relevancyState
            )

            StandardVerticalSpacing()
        }

        HorizontalRule()

        if (conditionsState.value.isNotEmpty()) {
            ConditionStateList(activeTab = activeTab, state = conditionsState)
            HorizontalRule()
        }

        StandardVerticalSpacing()

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    Timber.v("Save filter")
                    save()
                },
                enabled = canAddConditionState.value
            ) {
                Text(text = LocalContext.current.getString(R.string.save))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    save()

                    Timber.v("Add condition")
                    activeTab.value = LocatorEditCondition(
                        idState.value
                    )
                },
                enabled = idState.value > 0
            ) {
                Text(text = LocalContext.current.getString(R.string.add_condition))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun RelevancySlider(
    state: MutableState<Int>
) {
    Column {
        Text(
            text = state.value.relevancyString()
        )
    }
    Slider(
        modifier = Modifier.padding(horizontal = 16.dp),
        value = state.value.toFloat(),
        onValueChange = { value ->
            state.value = value.roundToInt()
        },
        valueRange = 0f..5f,
        steps = 6,
        colors = SliderDefaults.colors(
            activeTickColor = MaterialTheme.colors.secondaryVariant,
            activeTrackColor = MaterialTheme.colors.secondaryVariant,
            inactiveTickColor = MaterialTheme.colors.primaryVariant,
            inactiveTrackColor = MaterialTheme.colors.primaryVariant,
            thumbColor = MaterialTheme.colors.secondaryVariant
        )
    )
}

@Composable
fun AddFilterButton(
    modifier: Modifier = Modifier,
    party: Party? = null,
    quest: Quest? = null
) {
    val dialogState = remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = {
            dialogState.value = true
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.filter_plus),
            contentDescription = LocalContext.current.getString(R.string.add_filter)
        )
    }

    AddFilterDialog(party = party, quest = quest, state = dialogState)
}

@Composable
fun AddFilterDialog(
    party: Party? = null,
    quest: Quest? = null,
    state: MutableState<Boolean>
) {
    if (state.value) {
        val vm: FilterViewModel by viewModel()

        AlertDialog(
            onDismissRequest = {
                state.value = false
            },
            title = {
                Text(text = LocalContext.current.getString(R.string.add_filter))
            },
            text = {
                val scrollState = rememberScrollState()
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    party?.server?.let { serverName ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    vm.saveFilter(FilterConditions.server(serverName))
                                }
                                state.value = false
                            }
                        ) {
                            Text(
                                text = LocalContext.current.getString(
                                    R.string.add_filter_server,
                                    serverName
                                )
                            )
                        }
                    }

                    quest?.name?.let { questName ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    vm.saveFilter(FilterConditions.quest(questName))
                                }
                                state.value = false
                            }
                        ) {
                            Text(
                                text = LocalContext.current.getString(
                                    R.string.add_filter_quest,
                                    questName
                                )
                            )
                        }
                    }
                    quest?.patron?.let { patron ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    vm.saveFilter(FilterConditions.patron(patron))
                                }
                                state.value = false
                            }
                        ) {
                            Text(
                                text = LocalContext.current.getString(
                                    R.string.add_filter_patron,
                                    patron
                                )
                            )
                        }
                    }


                    party?.quest?.name?.let { questName ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    vm.saveFilter(FilterConditions.quest(questName))
                                }
                                state.value = false
                            }
                        ) {
                            Text(
                                text = LocalContext.current.getString(
                                    R.string.add_filter_quest,
                                    questName
                                )
                            )
                        }
                    }
                    party?.quest?.adventureArea?.let { area ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    vm.saveFilter(FilterConditions.area(area))
                                }
                                state.value = false
                            }
                        ) {
                            Text(
                                text = LocalContext.current.getString(
                                    R.string.add_filter_area,
                                    area
                                )
                            )
                        }
                    }
                    party?.players()?.mapNotNull { it.guild }?.toSet()?.filter { it.isNotBlank() }
                        ?.forEach { guildName ->
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        vm.saveFilter(FilterConditions.guild(guildName))
                                    }
                                    state.value = false
                                }
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    text = LocalContext.current.getString(
                                        R.string.add_filter_guild,
                                        guildName
                                    )
                                )
                            }
                        }
                }
            },
            confirmButton = {
                Button(onClick = {
                    state.value = false
                }) {
                    Text(text = LocalContext.current.getString(android.R.string.cancel))
                }
            }
        )
    }
}