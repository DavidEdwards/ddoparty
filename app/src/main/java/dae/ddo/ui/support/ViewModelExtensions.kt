package dae.ddo.ui.support

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import dae.ddo.MainActivity

@Composable
inline fun <reified T : ViewModel> viewModel() =
    (LocalContext.current as MainActivity).viewModels<T>()