package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Contributor
import com.autio.android_app.ui.stories.models.Narrator

sealed interface NarratorViewState
{
    data class FetchedNarrator(val narrator: Narrator) : NarratorViewState
    object FetchedNarratorFailed :NarratorViewState
    data class FetchedStoriesByContributor(val contributor: Contributor) : NarratorViewState
    object FetchedStoriesByContributorFailed :NarratorViewState
}
