package dae.ddo.ui.home

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dae.ddo.R
import dae.ddo.entities.Party
import dae.ddo.entities.Quest
import dae.ddo.utils.extensions.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = LocalContext.current.getString(R.string.loading))
        }
    }
}

@Composable
fun LabelledSwitch(
    text: String,
    checked: MutableState<Boolean>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                checked.value = !checked.value
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked.value,
            onCheckedChange = { active ->
                checked.value = active
            }
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            text = text
        )
    }
}

@Composable
fun HorizontalRule(
    color: Color = MaterialTheme.colors.primary,
    height: Int = 3
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp),
        color = color,
        elevation = 3.dp
    ) {}
}

@Composable
fun StandardVerticalSpacing() {
    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CardControls(optionsShown: Boolean = false, party: Party? = null, quest: Quest? = null) {
    AnimatedVisibility(visible = optionsShown) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                AddFilterButton(
                    modifier = Modifier,
                    party = party,
                    quest = quest
                )

                Spacer(modifier = Modifier.width(8.dp))

                (party?.quest?.name ?: quest?.name)?.let { linkSearch ->
                    WebLinkButton(
                        uri = "https://ddowiki.com/page/${
                            linkSearch.replace(
                                " ",
                                "_"
                            )
                        }".toUri()
                    )
                }
            }
        }
    }
}

@Composable
fun WebLinkButton(
    modifier: Modifier = Modifier,
    uri: Uri
) {
    val context = LocalContext.current
    IconButton(
        modifier = modifier,
        onClick = {
            context.launch(uri)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = LocalContext.current.getString(R.string.go_to_website)
        )
    }
}

@Composable
fun RepeatingLaunchedEffect(time: Long, block: suspend () -> Unit) {
    LaunchedEffect(true) {
        while (isActive) {
            delay(time)
            block.invoke()
        }
    }
}