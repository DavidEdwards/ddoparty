package dae.ddo.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dae.ddo.R
import dae.ddo.ui.support.viewModel
import dae.ddo.utils.extensions.launch
import dae.ddo.viewmodels.SettingsViewModel

sealed class Setting {
    data class Boolean(val key: String, val text: String, val default: kotlin.Boolean) :
        Setting()

    data class Link(val text: String, val url: String) : Setting()

    data class Text(val text: String) : Setting()

    companion object {
        const val NOTIFICATION_EXTREME_RELEVANCY = "SETTING_NOTIFICATION_EXTREME_RELEVANCY"
    }
}

@Preview
@Composable
fun Settings() {
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        HeaderRow(text = context.getString(R.string.notifications))
        SettingsRow(
            type = Setting.Boolean(
                text = context.getString(R.string.notify_for_extreme_relevancy),
                key = Setting.NOTIFICATION_EXTREME_RELEVANCY,
                default = false
            )
        )

        HeaderRow(text = context.getString(R.string.dungeons_dragons_online))
        SettingsRow(
            type = Setting.Text(
                text = context.getString(R.string.dungeons_dragons_copyright)
            )
        )

        HeaderRow(text = context.getString(R.string.data_sources))
        SettingsRow(
            type = Setting.Link(
                text = context.getString(R.string.data_source_api),
                url = "https://www.playeraudit.com/api/"
            )
        )
        SettingsRow(
            type = Setting.Link(
                text = context.getString(R.string.data_source_ddo),
                url = "https://www.ddo.com/en"
            )
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
fun HeaderRow(
    text: String
) {
    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    ) {
        Text(text = text, style = MaterialTheme.typography.h5)
    }
}

@Composable
fun SettingsRow(
    type: Setting
) {
    val vm: SettingsViewModel by viewModel()

    val context = LocalContext.current
    var rowClicked: (() -> Unit)? = null

    Row(
        modifier = Modifier
            .clickable {
                rowClicked?.invoke()
            }
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    ) {
        when (type) {
            is Setting.Boolean -> {
                val state by vm.get(type.key).collectAsState(initial = false)
                rowClicked = {
                    vm.set(type.key, !state)
                }
                BooleanSetting(text = type.text, state = state) {
                    vm.set(type.key, it)
                }
            }
            is Setting.Link -> {
                rowClicked = {
                    context.launch(type.url.toUri())
                }
                Text(text = type.text)
            }
            is Setting.Text -> {
                Text(text = type.text)
            }
        }
    }
}

@Composable
fun BooleanSetting(
    text: String,
    state: Boolean,
    set: (Boolean) -> Unit
) {
    Switch(
        checked = state,
        onCheckedChange = {
            set(it)
        }
    )

    Spacer(modifier = Modifier.width(16.dp))

    Text(text = text)
}
