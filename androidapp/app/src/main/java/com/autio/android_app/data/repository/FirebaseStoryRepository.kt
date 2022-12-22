package com.autio.android_app.data.repository

import com.autio.android_app.data.model.story.Story
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

class FirebaseStoryRepository {
    companion object {
        private val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        private val storiesRef: DatabaseReference = rootRef.child("stories")
        private val likesRef: DatabaseReference = rootRef.child("likes")

        suspend fun getStoriesFromRealtimeDatabase() : Response {
            val response = Response()
            try {
                response.stories = storiesRef.get().await().children.map {
                    it.getValue(Story::class.java)!!
                }
            } catch (e: Exception) {
                response.exception = e
            }
            return response
        }

        suspend fun getLikesByStoryId(storyId: String) : Response {
            val response = Response()
            try {
                response.likes =
                    likesRef.child(
                        storyId
                    )
                        .get()
                        .await().children.associate {
                            it.key!! to (it.value as Boolean)
                        }
            } catch (e: Exception) {
                response.exception = e
            }
            return response
        }
    }

    data class Response(
        var stories: List<Story>? = null,
        var likes: Map<String, Boolean>? = null,
        var exception: Exception? = null
    )
}