package com.autio.android_app.domain.mappers

import com.autio.android_app.data.api.model.account.CreateAccountDto
import com.autio.android_app.data.api.model.account.GuestResponse
import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.story.AuthorDto
import com.autio.android_app.data.api.model.story.ContributorResponse
import com.autio.android_app.data.api.model.story.NarratorDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.api.model.story.StoryDto
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.database.entities.UserEntity
import com.autio.android_app.ui.stories.models.AccountRequest
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Contributor
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.stories.models.Narrator
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.models.User

//TODO(need to break this up for readability)
fun CategoryEntity.toModel(): Category {
    return Category(id, title, order)
}

fun MapPointEntity.toModel(): Story {
    return Story(
        id,
        lat,
        lng,
        range,
        state ?: "",
        countryCode ?: "",
        title ?: "",
        description ?: "",
        narratorName ?: "",
        authorName ?: "",
        category ?: "",
    )
}

fun History.toMapPointEntity(): HistoryEntity {
    return HistoryEntity(storyId, playedAt)
}

fun Category.toMapPointEntity(): CategoryEntity {
    return CategoryEntity(id, title, order)
}

fun StoryDto.toModel(): Story {
    return Story(
        id,
        lat,
        lon,
        range,
        state,
        countryCode,
        title,
        description,
        narrator,
        author,
        category?.title.toString(), //Todo(Check this later)
        imageUrl,
        recordUrl,
        duration,
        isLiked,
        listenedAt,
        modifiedDate,
        isBookmarked,
        listenedAtLeast30Secs,
        isDownloaded,
        publishedDate
    )
}

fun Story.toMapPointEntity(): MapPointEntity {
    return MapPointEntity(
        id,
        publishedAt = 0,
        fbid = "",
        lat,
        lng,
        range,
        state,
        countryCode,
        title,
        description,
        narrator,
        author,
        category.toString()
    )
}

fun Story.toStoryEntity(): StoryEntity {
    return StoryEntity(
        id,
        title,
        description,
        lat,
        lng,
        range,
        imageUrl,
        recordUrl,
        authorId = 0,
        duration,
        category,
        publishedDate,
        modifiedDate,
        imageAttribution = "",
        narrator,
        author,
        state,
        countryCode,
        privateId = "",
        isLiked,
        isBookmarked,
        isDownloaded,
        listenedAt,
        listenedAtLeast30Secs,
    )
}

fun StoryEntity.toModel(): Story {
    return Story(
        id,
        lat,
        lon,
        range,
        state,
        countryCode,
        title,
        description,
        narrator,
        author,
        category,
        imageUrl,
        recordUrl,
        duration,
        isLiked,
        listenedAt,
        modifiedDate,
        isBookmarked,
        listenedAtLeast30Secs,
        isDownloaded,
        publishedDate
    )
}


fun Story.toDto(): StoryDto {
    return StoryDto(
        id,
        title,
        description,
        lat,
        lng,
        range,
        imageUrl,
        recordUrl,
        authorId = 0,
        duration,
        category?.let { Category(0, it, 0) }, //TODO(Check this later)
        publishedDate,
        modifiedDate,
        imageAttribution = "",
        narrator,
        author,
        state,
        countryCode,
        privateId = "",
        isLiked,
        isBookmarked,
        isDownloaded,
        listenedAt,
        listenedAtLeast30Secs,
    )
}

fun UserEntity.toModel(): User {
    return User(
        userId,
        userName,
        userEmail,
        userApiToken,
        isGuestUser,
        remainingStories,
        isPremiumUser
    )
}

fun Story.toPlaysDto(
    wasPresent: Boolean = false,
    autoPlay: Boolean = false,
    isDownloaded: Boolean = false,
    connection: String = "",
): PlaysDto {
    return PlaysDto(
        id,
        wasPresent = wasPresent,
        autoPlay = autoPlay,
        isDownloaded = isDownloaded,
        connection = connection,
        lat,
        lng
    )

}

fun GuestResponse.toModel(): User {
    return User(
        id, name = "", email = "", apiToken, isGuest
    )
}

fun LoginResponse.toModel(): User {
    return User(
        id, name, email, apiToken, isGuest
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id, name, email, apiToken, isGuest, remainingStories, isPremiumUser
    )
}

fun LoginRequest.toDTO(): LoginDto {
    return LoginDto(
        email, password
    )

}

fun AccountRequest.toDTO(): CreateAccountDto {
    return CreateAccountDto(
        email, emailConfirmation, password, passwordConfirmation, name
    )
}

fun Author.toDto(): AuthorDto {
    return AuthorDto(
        id, name, biography, url, imageUrl
    )
}

fun AuthorDto.toModel(): Author {
    return Author(
        id, name, biography, url, imageUrl
    )


}


fun ContributorResponse.toModel(): Contributor {
    return Contributor(
        currentPage, data, totalPages
    )
}

fun NarratorDto.toModel(): Narrator {
    return Narrator(
        id,
        name,
        biography,
        url,
        imageUrl
    )
}
