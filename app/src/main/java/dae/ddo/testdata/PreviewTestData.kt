package dae.ddo.testdata

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dae.ddo.entities.Party
import dae.ddo.entities.Quest
import dae.ddo.ui.data.PartyUiData

class SamplePartyUiDataProvider : PreviewParameterProvider<List<PartyUiData>> {
    override val values = sequenceOf(
        listOf(
            PartyUiData(
                party = Party.sample(1),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(2),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(3),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(4),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(5),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(6),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(7),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(8),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(9),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            ),
            PartyUiData(
                party = Party.sample(10),
                relevancy = (0..1).random(),
                maxRelevancy = 1,
                matchedFilters = listOf()
            )
        )
    )
    override val count: Int = values.count()
}

class SampleQuestProvider : PreviewParameterProvider<List<Quest>> {
    override val values = sequenceOf(
        listOf(
            Quest.sample(1),
            Quest.sample(2),
            Quest.sample(3),
            Quest.sample(4),
            Quest.sample(5),
            Quest.sample(6),
            Quest.sample(7),
            Quest.sample(8),
            Quest.sample(9)
        )
    )
    override val count: Int = values.count()
}