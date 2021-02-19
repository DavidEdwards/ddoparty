package dae.ddo.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dae.ddo.R
import dae.ddo.entities.Condition
import dae.ddo.entities.ConditionType
import dae.ddo.state.LocatorEditCondition
import dae.ddo.state.LocatorEditFilter
import dae.ddo.state.NavigationLocator
import dae.ddo.ui.support.viewModel
import dae.ddo.viewmodels.FilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ConditionStateList(
    activeTab: MutableState<NavigationLocator>,
    state: State<List<Condition>>
) {
    val conditions = state.value
    ConditionList(activeTab = activeTab, conditions = conditions)
}

@Composable
fun ConditionList(
    activeTab: MutableState<NavigationLocator>,
    conditions: List<Condition>
) {
    if (conditions.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            conditions.forEachIndexed { index, condition ->
                if (index > 0) {
                    Divider()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            activeTab.value = LocatorEditCondition(
                                filterId = condition.filterId,
                                conditionId = condition.id
                            )
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = "${index + 1}. ${condition.type}"
                    )
                    Text(
                        modifier = Modifier,
                        text = condition.arg1,
                        style = MaterialTheme.typography.overline
                    )
                }

            }
        }
    } else {
        LoadingIndicator()
    }
}

@Composable
fun EditCondition(
    activeTab: MutableState<NavigationLocator>,
    filterId: Long,
    conditionId: Long? = null
) {
    val vm: FilterViewModel by viewModel()
    val scope = rememberCoroutineScope()

    val canAddConditionState = remember { mutableStateOf(false) }
    val idState = remember { mutableStateOf(0L) }
    val typeState = remember { mutableStateOf(ConditionType.NONE) }
    val arg1State = remember { mutableStateOf("") }
    val arg2State = remember { mutableStateOf(0) }
    val arg3State = remember { mutableStateOf(30) }
    val arg4State = remember { mutableStateOf(0f) }
    val arg5State = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = "conditionLoad,$filterId,$conditionId") {
        val condition = withContext(Dispatchers.IO) {
            conditionId?.let { vm.getCondition(conditionId).firstOrNull() }
                ?: Condition.dummy()
        }

        condition.apply {
            idState.value = condition.id
            typeState.value = condition.type
            arg1State.value = condition.arg1
            arg2State.value = condition.arg2
            arg3State.value = condition.arg3
            arg4State.value = condition.arg4
            arg5State.value = condition.arg5
            canAddConditionState.value =
                condition.filterId > 0 && condition.type != ConditionType.NONE
        }
    }

    val scrollState = rememberScrollState()

    fun validate() {
        canAddConditionState.value = typeState.value != ConditionType.NONE
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        ConditionTypeTabs(conditionTypeTabState = typeState, onSelected = { tab ->
            validate()
        })

        StandardVerticalSpacing()

        ConditionTypeFields(
            typeState = typeState,
            arg1State = arg1State,
            arg2State = arg2State,
            arg3State = arg3State,
            arg4State = arg4State,
            arg5State = arg5State,
            validate = ::validate
        )

        StandardVerticalSpacing()

        HorizontalRule()

        StandardVerticalSpacing()

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    Timber.v("Save filter")

                    val saveCondition = Condition(
                        id = idState.value,
                        filterId = filterId,
                        type = typeState.value,
                        arg1 = arg1State.value,
                        arg2 = arg2State.value,
                        arg3 = arg3State.value,
                        arg4 = arg4State.value,
                        arg5 = arg5State.value
                    )

                    scope.launch {
                        vm.saveCondition(saveCondition)

                        activeTab.value = LocatorEditFilter(
                            filterId = filterId
                        )
                    }
                },
                enabled = canAddConditionState.value
            ) {
                Text(text = LocalContext.current.getString(R.string.save))
            }
        }


    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConditionTypeTabs(
    conditionTypeTabState: MutableState<ConditionType>,
    onSelected: ((selected: ConditionType) -> Unit)? = null
) {
    val tabs = ConditionType.values() //.filter { it != ConditionType.NONE }

    val tabState = conditionTypeTabState.value

    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = tabState.ordinal
    ) {
        tabs.forEach { tab ->
            ConditionTypeTab(
                tab = tab,
                conditionTypeTabState = conditionTypeTabState,
                onSelected = onSelected
            )
        }
    }
}

@Composable
fun ConditionTypeTab(
    tab: ConditionType,
    conditionTypeTabState: MutableState<ConditionType>,
    onSelected: ((selected: ConditionType) -> Unit)? = null
) {
    Tab(
        modifier = Modifier.padding(16.dp),
        selected = conditionTypeTabState.value == tab,
        onClick = {
            conditionTypeTabState.value = tab
            onSelected?.invoke(tab)
        }
    ) {
        Text(text = LocalContext.current.getString(tab.readable))
    }
}

@Composable
fun ConditionTypeFields(
    typeState: MutableState<ConditionType>,
    arg1State: MutableState<String>,
    arg2State: MutableState<Int>,
    arg3State: MutableState<Int>,
    arg4State: MutableState<Float>,
    arg5State: MutableState<Boolean>,
    validate: () -> Unit
) {
    when (typeState.value) {
        ConditionType.NONE -> {
            Text(text = LocalContext.current.getString(R.string.choose_condition))
        }
        ConditionType.SERVER -> {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = arg1State.value,
                onValueChange = { name ->
                    arg1State.value = name
                    validate()
                },
                label = {
                    Text(text = LocalContext.current.getString(R.string.server_name))
                },
                placeholder = {
                    Text(text = LocalContext.current.getString(R.string.what_is_name_server))
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }
        ConditionType.GUILD -> {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = arg1State.value,
                onValueChange = { name ->
                    arg1State.value = name
                    validate()
                },
                label = {
                    Text(text = LocalContext.current.getString(R.string.guild_name))
                },
                placeholder = {
                    Text(text = LocalContext.current.getString(R.string.what_is_name_guild))
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }
        ConditionType.PLAYER -> {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = arg1State.value,
                onValueChange = { name ->
                    arg1State.value = name
                    validate()
                },
                label = {
                    Text(text = LocalContext.current.getString(R.string.player_name))
                },
                placeholder = {
                    Text(text = LocalContext.current.getString(R.string.what_is_name_player))
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }
        ConditionType.LEVEL -> {
            Text(
                text = LocalContext.current.getString(
                    R.string.minimum_level_x,
                    arg2State.value
                )
            )
            Slider(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = arg2State.value.toFloat(),
                onValueChange = { value ->
                    arg2State.value = value.roundToInt()
                    arg3State.value = max(value.roundToInt(), arg3State.value)
                },
                valueRange = 1f..30f,
                steps = 30,
                colors = SliderDefaults.colors(
                    activeTickColor = MaterialTheme.colors.primaryVariant,
                    activeTrackColor = MaterialTheme.colors.primaryVariant,
                    inactiveTickColor = MaterialTheme.colors.secondaryVariant,
                    inactiveTrackColor = MaterialTheme.colors.secondaryVariant,
                    thumbColor = MaterialTheme.colors.secondaryVariant
                )
            )

            Text(
                text = LocalContext.current.getString(
                    R.string.maximum_level_x,
                    arg3State.value
                )
            )

            Slider(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = arg3State.value.toFloat(),
                onValueChange = { value ->
                    arg3State.value = value.roundToInt()
                    arg2State.value = min(value.roundToInt(), arg2State.value)
                },
                valueRange = 1f..30f,
                steps = 30,
                colors = SliderDefaults.colors(
                    activeTickColor = MaterialTheme.colors.secondaryVariant,
                    activeTrackColor = MaterialTheme.colors.secondaryVariant,
                    inactiveTickColor = MaterialTheme.colors.primaryVariant,
                    inactiveTrackColor = MaterialTheme.colors.primaryVariant,
                    thumbColor = MaterialTheme.colors.secondaryVariant
                )
            )
        }
        ConditionType.QUEST -> {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = arg1State.value,
                onValueChange = { name ->
                    arg1State.value = name
                    validate()
                },
                label = {
                    Text(text = LocalContext.current.getString(R.string.quest_name))
                },
                placeholder = {
                    Text(text = LocalContext.current.getString(R.string.what_is_name_quest))
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface
                )
            )
        }
        ConditionType.RAID -> {
            LabelledSwitch(
                text = "The quest is a raid",
                checked = arg5State
            )
        }
        ConditionType.DIFFICULTY -> {
            Text(text = "Replace with radio options for normal, hard, elite, reaper")
        }
    }
}