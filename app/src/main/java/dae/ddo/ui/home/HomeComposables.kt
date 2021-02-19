package dae.ddo.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dae.ddo.R
import dae.ddo.state.*
import dae.ddo.ui.support.viewModel
import dae.ddo.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun Home() {
    val activeTab = remember { mutableStateOf<NavigationLocator>(LocatorTabParties) }
    val toolbarTitle = remember { mutableStateOf("Quest Finder") }

    Scaffold(
        topBar = {
            TopBar(activeTab = activeTab, appBarTitle = toolbarTitle)
        },
        content = {
            BodyContent(activeTab = activeTab, toolbarTitle = toolbarTitle)
        },
        bottomBar = {
            BottomBar(activeTab = activeTab)
        },
        floatingActionButton = {
            FloatingAction(activeTab = activeTab)
        }
    )
}

@Composable
fun TopBar(
    activeTab: MutableState<NavigationLocator>,
    appBarTitle: MutableState<String>
) {
    val vm: HomeViewModel by viewModel()
    val tab = activeTab.value
    val scope = rememberCoroutineScope()
    val networkState = vm.networkState().collectAsState(initial = NetworkState.None)

    TopAppBar(
        title = {
            Text(
                text = appBarTitle.value
            )
        },
        elevation = 4.dp,
        navigationIcon = when (tab) {
            is LocatorEditFilter -> ({
                IconButton(onClick = {
                    activeTab.value = LocatorTabFilters
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalContext.current.getString(R.string.back)
                    )
                }
            })
            is LocatorEditCondition -> ({
                IconButton(onClick = {
                    activeTab.value = LocatorEditFilter(tab.filterId)
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalContext.current.getString(R.string.back)
                    )
                }
            })
            is LocatorSettings -> ({
                IconButton(onClick = {
                    activeTab.value = LocatorTabParties
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalContext.current.getString(R.string.back)
                    )
                }
            })
            else -> ({
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = LocalContext.current.getString(R.string.back)
                )
            })
        },
        actions = {
            if (networkState.value == NetworkState.Loading) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colors.secondary,
                        strokeWidth = 2.dp
                    )
                }
            }

            when (tab) {
                is LocatorEditFilter -> {
                    IconButton(onClick = {
                        scope.launch {
                            vm.deleteFilter(tab.filterId)
                            activeTab.value = LocatorTabFilters
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = LocalContext.current.getString(R.string.delete_filter)
                        )
                    }
                }
                is LocatorEditCondition -> {
                    IconButton(onClick = {
                        scope.launch {
                            vm.deleteCondition(tab.conditionId)
                            activeTab.value = LocatorEditFilter(tab.filterId)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = LocalContext.current.getString(R.string.delete_condition)
                        )
                    }
                }
            }

            IconButton(onClick = {
                activeTab.value = LocatorSettings
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = LocalContext.current.getString(R.string.settings)
                )
            }
        }
    )
}

@Composable
fun BodyContent(
    activeTab: MutableState<NavigationLocator>,
    toolbarTitle: MutableState<String>
) {
    val tab = activeTab.value
    Column {
        when (tab) {
            is LocatorTabParties -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.party_finder)
                PartyScreen()
            }
            is LocatorTabQuests -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.quest_compendium)
                QuestList()
            }
            is LocatorTabFilters -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.filters)
                FilterList(activeTab)
            }
            is LocatorEditFilter -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.edit_filter)
                EditFilter(activeTab, filterId = tab.filterId)
            }
            is LocatorEditCondition -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.edit_condition)
                EditCondition(activeTab, filterId = tab.filterId, conditionId = tab.conditionId)
            }
            is LocatorSettings -> {
                toolbarTitle.value = LocalContext.current.getString(R.string.settings)
                Settings()
            }
        }
    }
}

@Composable
fun BottomBar(activeTab: MutableState<NavigationLocator>) {
    BottomNavigation(
        elevation = 4.dp
    ) {
        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.playlist_star),
                contentDescription = LocalContext.current.getString(R.string.parties)
            )
        }, selected = activeTab.value == LocatorTabParties, onClick = {
            activeTab.value = LocatorTabParties
        })

        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.horse_human),
                contentDescription = LocalContext.current.getString(R.string.quests)
            )
        }, selected = activeTab.value == LocatorTabQuests, onClick = {
            activeTab.value = LocatorTabQuests
        })

        BottomNavigationItem(icon = {
            Icon(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = LocalContext.current.getString(R.string.filters)
            )
        }, selected = activeTab.value == LocatorTabFilters, onClick = {
            activeTab.value = LocatorTabFilters
        })
    }
}

@Composable
fun FloatingAction(activeTab: MutableState<NavigationLocator>) {
    Column {
        val tab = activeTab.value
        when (tab) {
            LocatorTabParties -> {
            }
            LocatorTabQuests -> {
            }
            LocatorTabFilters -> FloatingActionButton(
                onClick = {
                    activeTab.value = LocatorEditFilter()
                },
                elevation = elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.filter_plus),
                    contentDescription = LocalContext.current.getString(R.string.add)
                )
            }
        }
    }
}





