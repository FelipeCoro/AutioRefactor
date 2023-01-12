package com.autio.android_app.ui.paging_sources

//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.autio.android_app.data.model.api_response.ContributorStoryData
//import com.autio.android_app.data.repository.ApiService
//import java.io.IOException
//
//class AuthorStoriesPagingSource(
//    private val service: ApiService,
//    private val userId: Int,
//    private val apiToken: String,
//    private val authorId: Int
//) : PagingSource<Int, ContributorStoryData>() {
//
//    override suspend fun load(
//        params: LoadParams<Int>
//    ): LoadResult<Int, ContributorStoryData> {
//        return try {
//            // Start refresh at page 1 if undefined.
//            val nextPageNumber =
//                params.key
//                    ?: 1
//            val response =
//                service.getStoriesByContributor(
//                    userId,
//                    "Bearer $apiToken",
//                    authorId,
//                    nextPageNumber
//                )
//            LoadResult.Page(
//                data = response.data,
//                prevKey = null, // Only paging forward.
//                nextKey = if (response.currentPage < response.totalPages) response.currentPage + 1 else null
//            )
//        } catch (e: IOException) {
//            return LoadResult.Error(e)
//        } catch (e: HttpException) {
//            return LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(
//        state: PagingState<Int, ContributorStoryData>
//    ): Int? {
//        // Try to find the page key of the closest page to anchorPosition, from
//        // either the prevKey or the nextKey, but you need to handle nullability
//        // here:
//        //  * prevKey == null -> anchorPage is the first page.
//        //  * nextKey == null -> anchorPage is the last page.
//        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
//        //    just return null.
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage =
//                state.closestPageToPosition(
//                    anchorPosition
//                )
//            anchorPage?.prevKey?.plus(
//                1
//            )
//                ?: anchorPage?.nextKey?.minus(
//                    1
//                )
//        }
//    }
//}