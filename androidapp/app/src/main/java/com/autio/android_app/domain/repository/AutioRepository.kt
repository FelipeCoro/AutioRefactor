package com.autio.android_app.domain.repository

import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Story
import kotlinx.coroutines.flow.Flow

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val allStories: Flow<List<Story>>
    fun login(ingredientName: String): Flow<Result<com.autio.android_app.data.api.model.account.LoginResponse>>
}
