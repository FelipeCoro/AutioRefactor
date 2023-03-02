package com.autio.android_app.domain.repository

import com.autio.android_app.data.model.account.LoginResponse
import kotlinx.coroutines.flow.Flow

interface AutioRepository {

    fun login(ingredientName: String): Flow<Result<LoginResponse>>

}
