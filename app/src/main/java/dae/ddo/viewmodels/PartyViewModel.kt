package dae.ddo.viewmodels

import androidx.lifecycle.ViewModel
import dae.ddo.entities.FilterConditions
import dae.ddo.entities.Party
import dae.ddo.entities.match
import dae.ddo.repositories.MainRepository
import dae.ddo.state.NetworkState
import dae.ddo.ui.data.PartyUiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import javax.inject.Inject

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    fun networkState(): StateFlow<NetworkState> = mainRepository.networkState

    fun parties() = mainRepository.parties()
    fun filtersAndConditions() = mainRepository.filtersAndConditions()

    fun sortRelevancy(
        list: List<Party>,
        filters: List<FilterConditions>,
        maxAgeInMinutes: Int = 120
    ): List<PartyUiData> {
        val now = Instant.now()

        val filtersWithConditions = filters
            .filter { it.filter.enabled }
            .filter { it.conditions.isNotEmpty() }

        val parties = list.filter { party ->
            val minutesOld = Duration.between(party.updated, now).toMinutes()
            val requiredFilters = filtersWithConditions.filter { filter -> filter.filter.mustMatch }
            return@filter requiredFilters.match(party) && minutesOld < maxAgeInMinutes
        }

        val data = parties.mapIndexed { index, party ->
            val relevancy = filtersWithConditions.sumBy { fc ->
                if (
                    fc.conditions.all { condition ->
                        condition.match(party)
                    }
                ) fc.filter.relevancy else 0
            }

            val matchingFilters = filtersWithConditions.filter {
                it.match(party)
            }

            PartyUiData(
                party, relevancy, 0, matchingFilters
            )
        }

        val maxRelevancy = data.maxOfOrNull { it.relevancy } ?: 0

        data.forEach { uiData ->
            uiData.maxRelevancy = maxRelevancy
        }

        return data
    }

}