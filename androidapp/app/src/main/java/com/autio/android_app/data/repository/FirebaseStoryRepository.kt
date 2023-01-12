package com.autio.android_app.data.repository

import android.util.Log
import com.autio.android_app.data.model.bookmarks.Bookmark
import com.autio.android_app.data.model.history.History
import com.autio.android_app.data.model.likes.Like
import com.autio.android_app.data.model.story.CategoryResponse
import com.autio.android_app.data.model.story.Story
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.format.DateTimeFormatter

class FirebaseStoryRepository {
    companion object {
        private val rootRef =
            FirebaseDatabase.getInstance().reference
        private val categoryRef =
            rootRef.child(
                "categories"
            )
        private val bookmarksRef =
            rootRef.child(
                "library"
            )
        private val likesRef =
            rootRef.child(
                "likes"
            )
        private val playedHistoryRef =
            rootRef.child(
                "playedHistory"
            )
        private val storiesRef =
            rootRef.child(
                "stories"
            )

        suspend fun getStoriesAfterModifiedDate(
            modifiedDate: Int
        ): Array<Story> {
            val storiesQuery =
                storiesRef.orderByChild(
                    "dateModifiedTimestamp"
                )
                    .startAfter(
                        modifiedDate.toDouble()
                    )
                    .get()

            return storiesQuery
                .await().children.mapNotNull {
                    val story =
                        it.getValue(
                            Story::class.java
                        )
                    story?.id =
                        it.key!!
                    story?.category =
                        CategoryResponse(
                            it.child(
                                "categoryId"
                            ).value as? String
                                ?: "",
                        )
                    story
                }
                .toTypedArray()
        }

        suspend fun getUserBookmarks(
            userId: String
        ): Array<Bookmark> {
            return bookmarksRef.child(
                userId
            )
                .get()
                .await().children.map {
                    Bookmark(
                        storyId = it.key!!,
                        isOwn = it.child(
                            "isOwn"
                        ).value as? String
                            ?: "true",
                        title = it.child(
                            "title"
                        ).value as? String
                            ?: ""
                    )
                }
                .toTypedArray()
        }

        suspend fun getCategory(
            categoryId: String?,
            onSuccessListener: ((CategoryResponse) -> Unit)? = null,
        ) {
            try {
                if (categoryId == null) return
                val category =
                    categoryRef.child(
                        categoryId
                    )
                        .get()
                        .await()
                if (category.exists()) onSuccessListener?.invoke(
                    CategoryResponse(
                        category.key!!,
                        category.child(
                            "title"
                        ).value as? String
                            ?: "",
                        category.child(
                            "order"
                        ).value as? Int
                            ?: 0
                    )
                )
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
            }
        }

        suspend fun isStoryBookmarkedByUser(
            userId: String,
            storyId: String
        ) =
            callbackFlow {
                val valueListener =
                    object :
                        ValueEventListener {
                        override fun onCancelled(
                            error: DatabaseError
                        ) {
                            close()
                        }

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {
                            trySend(
                                SnapshotResult(
                                    snapshot,
                                    null
                                )
                            )
                        }
                    }

                bookmarksRef.child(
                    userId
                )
                    .child(
                        storyId
                    )
                    .addValueEventListener(
                        valueListener
                    )

                awaitClose {
                    bookmarksRef.child(
                        userId
                    )
                        .child(
                            storyId
                        )
                        .removeEventListener(
                            valueListener
                        )
                }
            }

        fun bookmarkStory(
            userId: String,
            storyId: String,
            storyTitle: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            val map =
                hashMapOf(
                    "isOwn" to "true",
                    "title" to storyTitle
                )
            try {
                bookmarksRef.child(
                    userId
                )
                    .child(
                        storyId
                    )
                    .setValue(
                        map
                    )
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
                onFailureListener?.invoke()
            }
        }

        fun removeBookmark(
            userId: String,
            storyId: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                bookmarksRef.child(
                    userId
                )
                    .child(
                        storyId
                    )
                    .removeValue()
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
                onFailureListener?.invoke()
            }
        }

        suspend fun getUserFavoriteStories(
            userId: String
        ): Array<Like> {
            return likesRef.get()
                .await().children.filter {
                    it.hasChild(
                        userId
                    )
                }
                .associate {
                    it.key!! to (it.child(
                        userId
                    ).value as Boolean?)
                }
                .map {
                    Like(
                        storyId = it.key,
                        userId = userId,
                        isGiven = it.value
                    )
                }
                .toTypedArray()
        }

        suspend fun getLikesByStoryId(
            storyId: String
        ) =
            callbackFlow {
                val valueListener =
                    object :
                        ValueEventListener {
                        override fun onCancelled(
                            error: DatabaseError
                        ) {
                            close()
                        }

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {
                            trySend(
                                SnapshotResult(
                                    snapshot,
                                    null
                                )
                            )
                        }
                    }

                likesRef.child(
                    storyId
                )
                    .addValueEventListener(
                        valueListener
                    )

                awaitClose {
                    likesRef.child(
                        storyId
                    )
                        .removeEventListener(
                            valueListener
                        )
                }
            }

        fun giveLikeToStory(
            storyId: String,
            userId: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                likesRef.child(
                    storyId
                )
                    .child(
                        userId
                    )
                    .setValue(
                        true
                    )
                    .addOnCompleteListener { }
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
                onFailureListener?.invoke()
            }
        }

        fun removeLikeFromStory(
            storyId: String,
            userId: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                likesRef.child(
                    storyId
                )
                    .child(
                        userId
                    )
                    .setValue(
                        false
                    )
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                onFailureListener?.invoke()
            }
        }

        suspend fun getUserStoriesHistory(
            userId: String
        ): Array<History> {
            return playedHistoryRef.child(
                userId
            )
                .get()
                .await().children.map {
                    History(
                        it.key!!,
                        it.child(
                            "played_at"
                        ).value as? String
                            ?: ""
                    )
                }
                .toTypedArray()
        }

        fun addStoryToUserHistory(
            userId: String,
            storyId: String,
            onSuccessListener: ((String) -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                val listenedAt = DateTimeFormatter.ISO_INSTANT.format(
                    Instant.now()
                )
                playedHistoryRef.child(
                    userId
                )
                    .child(
                        storyId
                    )
                    .child(
                        "played_at"
                    )
                    .setValue(
                        DateTimeFormatter.ISO_INSTANT.format(
                            Instant.now()
                        )
                    )
                    .addOnSuccessListener {
                        onSuccessListener?.invoke(listenedAt)
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
            }
        }

        fun removeStoryFromUserHistory(
            userId: String,
            storyId: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                playedHistoryRef.child(
                    userId
                )
                    .child(
                        storyId
                    )
                    .removeValue()
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
                onFailureListener?.invoke()
            }
        }

        fun removeWholeUserHistory(
            userId: String,
            onSuccessListener: (() -> Unit)? = null,
            onFailureListener: (() -> Unit)? = null
        ) {
            try {
                playedHistoryRef.child(
                    userId
                )
                    .removeValue()
                    .addOnSuccessListener {
                        onSuccessListener?.invoke()
                    }
                    .addOnFailureListener {
                        onFailureListener?.invoke()
                    }
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStoryRepository",
                    "exception: ",
                    e
                )
                onFailureListener?.invoke()
            }
        }
    }

    data class SnapshotResult(
        val snapshot: DataSnapshot? = null,
        val error: Throwable? = null
    )
}