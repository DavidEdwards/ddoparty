package dae.ddo.ui.data

import dae.ddo.entities.FilterConditions
import dae.ddo.entities.Party

data class PartyUiData(
    val party: Party,
    val relevancy: Int,
    var maxRelevancy: Int,
    var matchedFilters: List<FilterConditions>
)
