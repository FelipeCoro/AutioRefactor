package com.autio.android_app.data.repository

import com.autio.android_app.data.model.account.LoginDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.domain.repository.AutioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor (
    private val autioRemoteDataSource: AutioRemoteDataSource
): AutioRepository {

    override fun login(loginDto: LoginDto): Flow<Result<LoginResponse>> {
        autioRemoteDataSource.login(loginDto)
    }

}
