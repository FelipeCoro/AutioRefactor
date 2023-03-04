package com.autio.android_app.data.repository

import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Story
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource
) : AutioRepository {

    override val userCategories: Flow<List<Category>>
        get() = autioRemoteDataSource.override
    val allStories: Flow<List<Story>>
        get() = TODO("Not yet implemented")

    override fun login(ingredientName: String): Flow<Result<LoginResponse>> {
        TODO("Not yet implemented")
    }

    override fun login(loginDto: com.autio.android_app.data.api.model.account.LoginDto): Flow<Result<com.autio.android_app.data.api.model.account.LoginResponse>> {
        autioRemoteDataSource.login(loginDto)
    }
}
