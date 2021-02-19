package dae.ddo.viewmodels

import androidx.lifecycle.ViewModel
import dae.ddo.repositories.MainRepository
import dae.ddo.state.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    fun networkState(): StateFlow<NetworkState> = mainRepository.networkState

    fun quests() = mainRepository.quests()

}