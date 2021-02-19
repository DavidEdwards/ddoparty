package dae.ddo.viewmodels

import androidx.lifecycle.ViewModel
import dae.ddo.entities.Condition
import dae.ddo.entities.FilterConditions
import dae.ddo.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    fun filtersAndConditions() = mainRepository.filtersAndConditions()
    fun conditions(filterId: Long) = mainRepository.conditions(filterId)

    suspend fun saveFilter(filter: FilterConditions): Long? {
        return mainRepository.saveFilter(filter)
    }

    suspend fun saveCondition(condition: Condition): List<Long> {
        return mainRepository.saveCondition(condition)
    }

    fun getFilterConditions(filterId: Long) = mainRepository.getFilterConditions(filterId)

    fun getCondition(conditionId: Long) = mainRepository.getCondition(conditionId)

}