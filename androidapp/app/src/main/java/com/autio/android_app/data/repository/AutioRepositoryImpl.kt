package com.autio.android_app.data.repository

import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.domain.repository.AutioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor (
    private val autioRemoteDataSource: AutioRemoteDataSource
): AutioRepository {

    override fun login(loginDto: com.autio.android_app.data.api.model.account.LoginDto): Flow<Result<com.autio.android_app.data.api.model.account.LoginResponse>> {
        autioRemoteDataSource.login(loginDto)
    }

}
