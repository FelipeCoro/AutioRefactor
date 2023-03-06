package com.autio.android_app.domain.mappers

import com.autio.android_app.data.api.model.account.CreateAccountDto
import com.autio.android_app.data.api.model.account.GuestResponse
import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.story.StoryDto
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.ui.stories.models.*

//TODO(need to break this up for readability)
fun CategoryEntity.toModel(): Category {
    return Category(id, firebaseId, title, order)
}

fun MapPointEntity.toModel(): Story {
    return Story(
        id,
        lat = lat,
        lon = lon,
        range = range,
        publishedDate = publishedAt,
        state = state,
        countryCode = countryCode
    )
}

fun History.toEntity(): HistoryEntity {
    return HistoryEntity(storyId, playedAt)
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id,
        firebaseId,
        title,
        order
    )
}

fun StoryDto.toModel(): Story {
    return Story(
        id,
        originalId,
        title,
        description,
        lat,
        lon,
        range,
        imageUrl,
        recordUrl,
        duration,
        publishedDate,
        modifiedDate,
        narrator,
        author,
        state,
        countryCode,
        category,
        isLiked,
        isBookmarked,
        isDownloaded,
        listenedAt,
        listenedAtLeast30Secs
    )
}

fun Story.toEntity(): MapPointEntity {
    return MapPointEntity(
        id,
        firebaseId = 0,
        publishedAt = 0,
        lat,
        lon,
        range,
        title,
        description,
        imageUrl = "",
        recordUrl,
        duration,
        state,
        countryCode,
        originalId,
        isLiked = 0,
        listenedAt = "",
        modifiedDate,
        isBookmarked,
        listenedAtLeast30Secs,
        isDownloaded
    )
}

fun Story.toDto(): StoryDto {
    return StoryDto(
        id,
        originalId,
        title,
        description,
        lat,
        lon,
        range,
        imageUrl,
        recordUrl,
        duration,
        publishedDate,
        modifiedDate,
        narrator,
        author,
        state,
        countryCode,
        category,
        isLiked,
        isBookmarked,
        isDownloaded,
        listenedAt,
        listenedAtLeast30Secs
    )
}

fun GuestResponse.toModel(): User {
    return User(
        id,
        name = "",
        email = "",
        apiToken,
        isGuest,
        firebaseKey
    )
}

fun LoginResponse.toModel(): User {
    return User(
        id,
        name = "",
        email = "",
        apiToken,
        isGuest,
        firebaseKey
    )
}

fun LoginRequest.toDTO(): LoginDto {
    return LoginDto(
        email,
        password
    )

}

fun AccountRequest.toDTO(): CreateAccountDto {
    return CreateAccountDto(
        email,
        emailConfirmation,
        password,
        passwordConfirmation,
        name
    )
}
