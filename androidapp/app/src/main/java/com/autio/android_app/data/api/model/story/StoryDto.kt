package com.autio.android_app.data.api.model.story

import androidx.room.*
import com.autio.android_app.data.api.model.pendings.Category
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// TODO: Update class to follow updated structure from API
// Here's an example of how it is being returned from the
// GET api/v1/library endpoint:
//"story": {
//            "id": 7,
//            "created_at": "2018-10-15T15:11:15.000000Z",
//            "updated_at": "2023-01-27T23:26:25.000000Z",
//            "title": "Powder Magazine",
//            "description": "A gun powder magazine & museum located in the historic center of Charleston.",
//            "cover_image_id": 0,
//            "firebase_identifier": "-MVLjZ16e1xPqq9JI-ug",
//            "firebase_key": null,
//            "range_in_meters": 3218,
//            "narration_filesize": 3140243,
//            "cover_image": "cover_images/63d014b801caf.jpg",
//            "cover_image_filesize": 63370,
//            "coordinate": {
//                "type": "Point",
//                "coordinates": [
//                    -79.930173600599,
//                    32.779399485363
//                ]
//            },
//            "attribution": "Visit Charleston",
//            "category_id": 1,
//            "author_id": 3,
//            "narrator_id": 4,
//            "duration_in_seconds": 110,
//            "narrator_name": "Bill Werlin",
//            "transcript": "The powder magazine is a gun powder magazine and museum located in the historic center of Charleston, on the south side of Cumberland street between church and meeting streets Completed in 1713, it is the oldest surviving public building in the former province of Carolina. It was used as a gunpowder store through the American revolutionary war and was declared a national historic landmark in 1989.\r\n\r\nIt is a single-story square structured with a stucco brick wall, 32 inches thick in an original red tile roof that is pyramidal with intersecting gables. Each wall of the building boasts a large arch, the walls get thinner as they reach the top of the arch changing from three feet thick near the ground to just a few inches thick near the top.\r\n\r\nThere are also few doors in the building so that in the event of an explosion, most of the explosive force would exit through the roof with the arches, acting like funnels. Sand stored in that roof would then smother and put out the fire. Construction of the building was authorized by the province of Carolina in 1703 during Queen Anne's war as part of a series of fortifications, but it was not completed until 1713.\r\n\r\nIt was used as a powder magazine until late in the revolutionary war, after which it saw a variety of other uses, including as a wine cellar for Gabriel Manigault. The local chapter of the National Society of The Colonial Dames of America acquired the building in 1902 and now operates it as a museum, which includes historic artifacts and displays about the building during the colonial and American revolution periods.",
//            "state": "SC",
//            "postal_code": "29401",
//            "tags": null,
//            "city": "Charleston",
//            "country_code": "USA",
//            "narration_url": "https://autio-staging.sfo2.cdn.digitaloceanspaces.com/narrations/63cffb67c186b.m4a"
//        }

/**
 * Base class which the whole app works around
 *
 * Stories are fetched from an API endpoint and then stored in
 * a room database for caching purposes
 * Another database table was created for downloaded stories in
 * device
 */
@Entity(
    tableName = "stories",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class StoryDto(
    // Firebase ID
    @PrimaryKey
    var id: String = "",
    // Story ID
    @get:PropertyName(
        "hhId"
    )
    @set:PropertyName(
        "hhId"
    )
    @SerializedName(
        "hhId"
    )
    var originalId: Int = 0,
    val title: String = "",
    val description: String = "",
    @get:PropertyName(
        "latitude"
    )
    @set:PropertyName(
        "latitude"
    )
    @SerializedName(
        "latitude"
    )
    var lat: Double = 0.0,
    @get:PropertyName(
        "longitude"
    )
    @set:PropertyName(
        "longitude"
    )
    @SerializedName(
        "longitude"
    )
    var lon: Double = 0.0,
    @get:PropertyName(
        "rangeInMeters"
    )
    @set:PropertyName(
        "rangeInMeters"
    )
    var range: Int = 0,
    @PropertyName(
        "imageURL"
    )
    @SerializedName(
        "imageURL"
    )
    val imageUrl: String = "",
    @PropertyName(
        "recordURL"
    )
    @SerializedName(
        "recordURL"
    )
    val recordUrl: String = "",
    @get:PropertyName(
        "durationInSeconds"
    )
    @set:PropertyName(
        "durationInSeconds"
    )
    var duration: Int = 0,
    @SerializedName(
        "dateAdded"
    )
    var publishedDate: Int = 0,
    @get:PropertyName(
        "dateModifiedTimestamp"
    ) @set:PropertyName(
        "dateModifiedTimestamp"
    )
    @SerializedName(
        "dateModified"
    )
    var modifiedDate: Int = 0,
    @get:PropertyName(
        "narratorName"
    ) @set:PropertyName(
        "narratorName"
    )
    @SerializedName(
        "narratorName"
    ) var narrator: String = "",
    @get:PropertyName(
        "authorName"
    ) @set:PropertyName(
        "authorName"
    )
    @SerializedName(
        "authorName"
    ) var author: String = "",
    val state: String = "",
    @SerializedName(
        "country_code"
    )
    val countryCode: String = "",
    @Embedded(
        prefix = "category_"
    )
    var category: Category? = null,
    val isLiked: Boolean? = null,
    val isBookmarked: Boolean? = null,
    val isDownloaded: Boolean? = null,
    val listenedAt: String? = null,
    val listenedAtLeast30Secs: Boolean? = null
) : Serializable
