package com.autio.android_app.data.repository.legacy

// import android.util.Log
// import com.autio.android_app.core.RetrofitHelper
// import com.autio.android_app.data.api.ApiClient
// import com.autio.android_app.data.api.model.account.*
// import com.autio.android_app.data.api.model.api_response.*
// import com.autio.android_app.data.api.model.bookmarks.AddBookmarkResponse
// import com.autio.android_app.data.api.model.bookmarks.RemoveBookmarkResponse
// import com.autio.android_app.data.api.model.category.StoryCategory
// import com.autio.android_app.data.api.model.history.AddHistoryResponse
// import com.autio.android_app.data.api.model.history.ClearHistoryResponse
// import com.autio.android_app.data.api.model.history.RemoveHistoryResponse
// import com.autio.android_app.data.api.model.narrator.Narrator
// import com.autio.android_app.data.api.model.story.*
// import com.autio.android_app.data.api.model.story.Story
// import retrofit2.Call
// import retrofit2.Callback
// import retrofit2.Response
//
// class ApiServiceOld {
// companion object {
// private val TAG =
// ApiServiceOld::class.simpleName
//
// private val retrofit =
// RetrofitHelper.buildService(
// ApiClient::class.java
// )
//
// // ACCOUNT
//
// /**
// * Gives access to user to call API requests based on a bearer token
// * and user's data included in the [LoginResponse]
// * @param loginDto email and password of an already existing user
// */
// fun login(
// loginDto: com.autio.android_app.data.api.model.account.LoginDto,
// onResult: (com.autio.android_app.data.api.model.account.LoginResponse?) -> Unit
// ) {
// retrofit.login(
// loginDto
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.LoginResponse> {
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.LoginResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
//
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.LoginResponse>,
// response: Response<com.autio.android_app.data.api.model.account.LoginResponse>
// ) {
// if (!response.isSuccessful) {
// onResult(
// null
// )
// } else {
// val userInfo =
// response.body()
// onResult(
// userInfo
// )
// }
// }
// })
// }
//
// /**
// * Authenticates a user as a guest
// * The difference from the [login] process is that the created user
// * has no data besides of a bearer token and an id for making limited requests to
// * server
// */
// fun loginAsGuest(
// onResult: (com.autio.android_app.data.api.model.account.GuestResponse?) -> Unit
// ) {
// retrofit.createGuestAccount()
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.GuestResponse> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.GuestResponse>,
// response: Response<com.autio.android_app.data.api.model.account.GuestResponse>
// ) {
// if (response.isSuccessful) {
// val guestInfo =
// response.body()
// onResult(
// guestInfo
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.GuestResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
// }
//
// /**
// * Creates an account for the user and then it follows the same process
// * as in [login]. If the [onResult] is given a "null" value,
// * it is assumed the email is already being used by an existing account.
// */
// fun createAccount(
// createAccountDto: com.autio.android_app.data.api.model.account.CreateAccountDto,
// onResult: (com.autio.android_app.data.api.model.account.LoginResponse?) -> Unit
// ) {
// retrofit.createAccount(
// createAccountDto
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.LoginResponse> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.LoginResponse>,
// response: Response<com.autio.android_app.data.api.model.account.LoginResponse>
// ) {
// if (response.isSuccessful) {
// val userInfo =
// response.body()
// onResult(
// userInfo
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.LoginResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
//
// })
// }
//
// /**
// * Update user's password
// * @param changePasswordDto includes the previous password, as well as the new one to use
// */
// fun changePassword(
// xUserId: Int,
// apiToken: String,
// changePasswordDto: com.autio.android_app.data.api.model.account.ChangePasswordDto,
// onResult: (com.autio.android_app.data.api.model.account.ChangePasswordResponse?) -> Unit
// ) =
// retrofit.changePassword(
// xUserId,
// "Bearer $apiToken",
// changePasswordDto
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.ChangePasswordResponse> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.ChangePasswordResponse>,
// response: Response<com.autio.android_app.data.api.model.account.ChangePasswordResponse>
// ) {
// if (response.isSuccessful) {
// val res =
// response.body()
// onResult(
// res
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.ChangePasswordResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
//
// fun deleteAccount(
// xUserId: Int,
// apiToken: String
// ) =
// retrofit.deleteAccount(
// xUserId,
// "Bearer $apiToken"
// )
//
// // PROFILE CALLS
//
// /**
// * Fetches user's data
// */
// fun getProfileData(
// xUserId: Int,
// apiToken: String,
// onResult: (com.autio.android_app.data.api.model.account.ProfileDto?) -> Unit
// ) =
// retrofit.getProfileDataV2(
// xUserId,
// "Bearer $apiToken",
// xUserId
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.ProfileDto> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.ProfileDto>,
// response: Response<com.autio.android_app.data.api.model.account.ProfileDto>
// ) {
// if (response.isSuccessful) {
// val userInfo =
// response.body()
// onResult(
// userInfo
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.ProfileDto>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// /**
// * Saves updatable data from a user (which can be either name and email
// * or the preferred order for story categories)
// * @param profileDto new data to be saved
// */
// fun updateProfile(
// xUserId: Int,
// apiToken: String,
// profileDto: com.autio.android_app.data.api.model.account.ProfileDto,
// onResult: (com.autio.android_app.data.api.model.account.ProfileDto?) -> Unit
// ) =
// retrofit.updateProfileV2(
// xUserId,
// "Bearer $apiToken",
// xUserId,
// profileDto
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.account.ProfileDto> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.account.ProfileDto>,
// response: Response<com.autio.android_app.data.api.model.account.ProfileDto>
// ) {
// if (response.isSuccessful) {
// val userInfo =
// response.body()
// onResult(
// userInfo
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.account.ProfileDto>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
//
// // STORIES CALLS
//
// /**
// * Captures stories from a list of [ids]
// */
// fun getStoriesByIds(
// xUserId: Int,
// apiToken: String,
// ids: List<Int>,
// onResult: (List<Story>?) -> Unit
// ) =
// retrofit.getStoriesByIds(
// xUserId,
// "Bearer $apiToken",
// ids
// )
// .enqueue(
// object :
// Callback<List<Story>> {
// override fun onResponse(
// call: Call<List<Story>>,
// response: Response<List<Story>>
// ) {
// if (response.isSuccessful) {
// val stories =
// response.body()
// onResult(
// stories
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<Story>>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// /**
// * Capture stories after a certain epoch [date]
// * This function should be used for capturing the stories
// * that will be shown in the maps' view
// */
// fun getStoriesAfterDate(
// xUserId: Int,
// apiToken: String,
// date: String,
// onResult: (List<Story>?) -> Unit
// ) =
// retrofit.getStoriesDiff(
// xUserId,
// "Bearer $apiToken",
// date
// )
// .enqueue(
// object :
// Callback<List<Story>> {
// override fun onResponse(
// call: Call<List<Story>>,
// response: Response<List<Story>>
// ) {
// Log.d(
// TAG,
// "response: ${response.code()}"
// )
// Log.d(
// TAG,
// "response: ${
// response.errorBody()
// ?.string()
// }"
// )
// if (response.isSuccessful) {
// val stories =
// response.body()
// onResult(
// stories
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<Story>>,
// t: Throwable
// ) {
// Log.d(
// TAG,
// "onFailure: $t"
// )
// onResult(
// null
// )
// }
// }
// )
//
// /**
// * Fetches a list from all the stories a contributor
// * has posted
// * It supports pagination for better performance
// */
// fun getStoriesByContributor(
// xUserId: Int,
// apiToken: String,
// contributorId: Int,
// page: Int,
// onResult: (com.autio.android_app.data.api.model.api_response.ContributorApiResponse?) -> Unit
// ) =
// retrofit.getStoriesByContributor(
// xUserId,
// "Bearer $apiToken",
// contributorId,
// page
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.api_response.ContributorApiResponse> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.api_response.ContributorApiResponse>,
// response: Response<com.autio.android_app.data.api.model.api_response.ContributorApiResponse>
// ) {
// if (response.isSuccessful) {
// val authorApiResponse =
// response.body()
// onResult(
// authorApiResponse
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.api_response.ContributorApiResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// /**
// * Captures profile data from an author
// */
// fun getAuthorOfStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (AuthorDto?) -> Unit
// ) =
// retrofit.getAuthorOfStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<AuthorDto> {
// override fun onResponse(
// call: Call<AuthorDto>,
// response: Response<AuthorDto>
// ) {
// if (response.isSuccessful) {
// val author =
// response.body()
// onResult(
// author
// )
// }
// }
//
// override fun onFailure(
// call: Call<AuthorDto>,
// t: Throwable
// ) =
// onResult(
// null
// )
// })
//
// /**
// * Captures profile data from a narrator
// */
// fun getNarratorOfStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (Narrator?) -> Unit
// ) =
// retrofit.getNarratorOfStory(
// xUserId,
// "Bearer $apiToken",
// storyId
// )
// .enqueue(
// object :
// Callback<Narrator> {
// override fun onResponse(
// call: Call<Narrator>,
// response: Response<Narrator>
// ) {
// if (response.isSuccessful) {
// val narrator =
// response.body()
// onResult(
// narrator
// )
// }
// }
//
// override fun onFailure(
// call: Call<Narrator>,
// t: Throwable
// ) =
// onResult(
// null
// )
// })
//
// /**
// * Gets the possible categories a story can be catalogued as
// */
// fun getStoryCategories(
// xUserId: Int,
// apiToken: String,
// onResult: (List<StoryCategory>?) -> Unit
// ) =
// retrofit.getCategories(
// xUserId,
// "Bearer $apiToken"
// )
// .enqueue(
// object :
// Callback<List<StoryCategory>> {
// override fun onResponse(
// call: Call<List<StoryCategory>>,
// response: Response<List<StoryCategory>>
// ) {
// if (response.isSuccessful) {
// val categories =
// response.body()
// onResult(
// categories
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<StoryCategory>>,
// t: Throwable
// ) =
// onResult(
// null
// )
// })
//
// fun postStoryPlayed(
// xUserId: Int,
// apiToken: String,
// playsDto: PlaysDto,
// onResult: (com.autio.android_app.data.api.model.api_response.PlaysResponse?) -> Unit
// ) =
// retrofit.postStoryPlayed(
// xUserId,
// "Bearer $apiToken",
// playsDto
// )
// .enqueue(
// object :
// Callback<com.autio.android_app.data.api.model.api_response.PlaysResponse> {
// override fun onResponse(
// call: Call<com.autio.android_app.data.api.model.api_response.PlaysResponse>,
// response: Response<com.autio.android_app.data.api.model.api_response.PlaysResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<com.autio.android_app.data.api.model.api_response.PlaysResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
//
// // FAVORITE CALLS
//
// fun likedStoriesByUser(
// xUserId: Int,
// apiToken: String,
// onResult: (List<Story>?) -> Unit
// ) =
// retrofit.likedStoriesByUser(
// xUserId,
// apiToken
// )
// .enqueue(
// object :
// Callback<List<Story>> {
// override fun onResponse(
// call: Call<List<Story>>,
// response: Response<List<Story>>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<Story>>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun isStoryLikedByUser(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (Boolean?) -> Unit
// ) =
// retrofit.isStoryLikedByUser(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<StoryLikedResponse> {
// override fun onResponse(
// call: Call<StoryLikedResponse>,
// response: Response<StoryLikedResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body?.isLiked
// )
// }
// }
//
// override fun onFailure(
// call: Call<StoryLikedResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun likesByStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (Int?) -> Unit
// ) =
// retrofit.likesByStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<StoryLikesResponse> {
// override fun onResponse(
// call: Call<StoryLikesResponse>,
// response: Response<StoryLikesResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body?.likes
// )
// }
// }
//
// override fun onFailure(
// call: Call<StoryLikesResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun likeStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (Boolean?) -> Unit
// ) =
// retrofit.giveLikeToStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<LikeResponse> {
// override fun onResponse(
// call: Call<LikeResponse>,
// response: Response<LikeResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body?.liked
// )
// }
// }
//
// override fun onFailure(
// call: Call<LikeResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
//
// fun dislikeStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (Boolean?) -> Unit
// ) =
// retrofit.removeLikeFromStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<LikeResponse> {
// override fun onResponse(
// call: Call<LikeResponse>,
// response: Response<LikeResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body?.liked
// )
// }
// }
//
// override fun onFailure(
// call: Call<LikeResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// })
//
// // HISTORY CALLS
//
// fun getHistory(
// xUserId: Int,
// apiToken: String,
// onResult: (List<Story>?) -> Unit
// ) =
// retrofit.getUserHistory(
// xUserId,
// apiToken
// )
// .enqueue(
// object :
// Callback<List<Story>> {
// override fun onResponse(
// call: Call<List<Story>>,
// response: Response<List<Story>>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<Story>>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun addStoryToHistory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (AddHistoryResponse?) -> Unit
// ) =
// retrofit.addStoryToHistory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<AddHistoryResponse> {
// override fun onResponse(
// call: Call<AddHistoryResponse>,
// response: Response<AddHistoryResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<AddHistoryResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun removeStoryFromHistory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (RemoveHistoryResponse?) -> Unit
// ) =
// retrofit.removeStoryFromHistory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<RemoveHistoryResponse> {
// override fun onResponse(
// call: Call<RemoveHistoryResponse>,
// response: Response<RemoveHistoryResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<RemoveHistoryResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun clearHistory(
// xUserId: Int,
// apiToken: String,
// onResult: (ClearHistoryResponse?) -> Unit
// ) =
// retrofit.clearHistory(
// xUserId,
// apiToken
// )
// .enqueue(
// object :
// Callback<ClearHistoryResponse> {
// override fun onResponse(
// call: Call<ClearHistoryResponse>,
// response: Response<ClearHistoryResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<ClearHistoryResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// // BOOKMARKS CALLS
//
// fun getStoriesFromUserBookmarks(
// xUserId: Int,
// apiToken: String,
// onResult: (List<Story>?) -> Unit
// ) =
// retrofit.getStoriesFromUserBookmarks(
// xUserId,
// apiToken
// )
// .enqueue(
// object :
// Callback<List<Story>> {
// override fun onResponse(
// call: Call<List<Story>>,
// response: Response<List<Story>>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<List<Story>>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun bookmarkStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (AddBookmarkResponse?) -> Unit
// ) =
// retrofit.bookmarkStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<AddBookmarkResponse> {
// override fun onResponse(
// call: Call<AddBookmarkResponse>,
// response: Response<AddBookmarkResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<AddBookmarkResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
//
// fun removeBookmarkFromStory(
// xUserId: Int,
// apiToken: String,
// storyId: Int,
// onResult: (RemoveBookmarkResponse?) -> Unit
// ) =
// retrofit.removeBookmarkFromStory(
// xUserId,
// apiToken,
// storyId
// )
// .enqueue(
// object :
// Callback<RemoveBookmarkResponse> {
// override fun onResponse(
// call: Call<RemoveBookmarkResponse>,
// response: Response<RemoveBookmarkResponse>
// ) {
// if (response.isSuccessful) {
// val body =
// response.body()
// onResult(
// body
// )
// }
// }
//
// override fun onFailure(
// call: Call<RemoveBookmarkResponse>,
// t: Throwable
// ) {
// onResult(
// null
// )
// }
// }
// )
// }
// }
