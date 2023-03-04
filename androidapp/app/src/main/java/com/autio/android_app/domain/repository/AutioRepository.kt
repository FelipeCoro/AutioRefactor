package com.autio.android_app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AutioRepository {
    fun login(ingredientName: String): Flow<Result<com.autio.android_app.data.api.model.account.LoginResponse>>
}
