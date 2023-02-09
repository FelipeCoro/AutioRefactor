package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.Category
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Story::class,
        DownloadedStory::class,
        Category::class
    ],
    version = 24
)
abstract class StoryDataBase :
    RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao
    abstract fun categoryDao(): CategoryDao

    private class StoryDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(
            db: SupportSQLiteDatabase
        ) {
            super.onCreate(
                db
            )
            scope.launch {
                INSTANCE?.let { database ->
                    val storyDao =
                        database.storyDao()
                    storyDao
                        .deleteAllStories()
//                    val data =
//                        mutableListOf<Story>()
//                    context.assets.open(
//                        "database/published_map_points.sqlite"
//                    )
//                        .use { inputStream ->
//                            InputStreamReader(
//                                inputStream,
//                                Charset.forName(
//                                    "UTF-8"
//                                )
//                            ).use { reader ->
//                                BufferedReader(
//                                    reader
//                                ).use { bufferedReader ->
//                                    var row =
//                                        bufferedReader.readLine()
//                                    while (row != null) {
//                                        val items =
//                                            row.split(
//                                                "\t"
//                                            )
//                                        Log.d(
//                                            "StoryDatabase",
//                                            items.joinToString(
//                                                "\n"
//                                            )
//                                        )
//                                        data.add(
//                                            Story(
//                                                originalId = items[0].toIntOrNull()
//                                                    ?: 0,
//                                                publishedDate = items[1].toIntOrNull()
//                                                    ?: 0,
//                                                id = items[2],
//                                                lat = items[3].toDoubleOrNull()
//                                                    ?: 0.0,
//                                                lon = items[4].toDoubleOrNull()
//                                                    ?: 0.0,
//                                                range = items[5].toIntOrNull()
//                                                    ?: 0,
//                                                state = items[6],
//                                                countryCode = items[7],
//                                                title = items[8],
//                                                description = items[9],
//                                                narrator = items[10],
//                                                author = items[11],
//                                                category = Category(
//                                                    title = items[12]
//                                                )
//                                            )
//                                        )
//                                        row =
//                                            bufferedReader.readLine()
//                                    }
//                                }
//                            }
//                        }
//                    withContext(
//                        Dispatchers.IO
//                    ) {
//                        storyDao.addStories(
//                            data.toTypedArray()
//                        )
//                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryDataBase? =
            null

        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): StoryDataBase =
            INSTANCE
                ?: synchronized(
                    this
                ) {
                    INSTANCE
                        ?: buildDatabase(
                            context,
                            scope
                        ).also {
                            INSTANCE =
                                it
                        }
                }

        private fun buildDatabase(
            context: Context,
            scope: CoroutineScope
        ): StoryDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                StoryDataBase::class.java,
                "STORIES"
            )
                .fallbackToDestructiveMigration()
                .addCallback(
                    StoryDatabaseCallback(
                        context,
                        scope
                    )
                )
                .enableMultiInstanceInvalidation()
                .build()
        }
    }
}