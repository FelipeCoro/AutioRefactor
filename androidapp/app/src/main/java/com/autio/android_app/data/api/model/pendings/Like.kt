package com.autio.android_app.data.api.model.pendings

/**
 * Data class that stores like information on a user
 * over a story. It is generally used for retrieving the
 * current user's favorite stories.
 *
 * @param storyId the story's id which this like was given to
 * @param userId the user's id owner of the like
 * @param isGiven is set to true or false, where a false value
 * indicates a like was previously given but it was later removed
 */
data class Like(
    val storyId: String,
    val userId: String,
    val isGiven: Boolean?
)
