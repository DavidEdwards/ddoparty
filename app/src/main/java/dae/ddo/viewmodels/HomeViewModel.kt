package dae.ddo.viewmodels

import androidx.lifecycle.ViewModel
import dae.ddo.repositories.MainRepository
import dae.ddo.state.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    fun networkState(): StateFlow<NetworkState> = mainRepository.networkState

    suspend fun deleteFilter(filterId: Long) {
        mainRepository.deleteFilter(filterId)
    }

    suspend fun deleteCondition(conditionId: Long) {
        mainRepository.deleteCondition(conditionId)
    }

}