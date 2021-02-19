package dae.ddo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.lifecycleScope
import dae.ddo.ui.DdoTheme
import dae.ddo.ui.home.Home
import dae.ddo.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DdoTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Home()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            if (mainViewModel.totalQuests() == 0) {
                Timber.v("No quest data is saved, refresh it.")
                mainViewModel.refreshQuests()
            }

            val duration = Duration.between(mainViewModel.lastPartyRefresh().first(), Instant.now())


            // TODO: Return this to cached checks once repeatOnLifecycle is added to lifecycle-ktx
//            if (duration.toMinutes() > PARTY_CACHE_TIME) {
//                Timber.v("Party data is older than 3 minutes. Refresh data.")
            while (isActive) {
                Timber.v("Timer triggered. Refresh data.")
                mainViewModel.refreshParties()
                delay(60000L)
            }
//            }
        }
    }

    companion object {
        private const val PARTY_CACHE_TIME = 3 // minutes
    }
}

