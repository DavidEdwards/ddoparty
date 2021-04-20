package dae.ddo.viewmodels

import androidx.lifecycle.ViewModel
import dae.ddo.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    suspend fun totalQuests() = mainRepository.totalQuests()
    suspend fun refreshQuests() = mainRepository.refreshQuests()
    suspend fun refreshParties() = mainRepository.refreshParties()

}