package com.autio.android_app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.StoryViewModel


fun Fragment.onOptionClicked(
    option: StoryOption,
    story: Story,
    storyViewModel: StoryViewModel,
    prefRepository: PrefRepository,
    activity: Activity,
    context: Context
) {
    showPaywallOrProceedWithNormalProcess(
        prefRepository,
        activity, false //TODO(CHANGE TO TRUE)
    ) {
        when (option) {
            StoryOption.BOOKMARK -> {

                storyViewModel.bookmarkStory(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    story.id
                )
            }
            StoryOption.REMOVE_BOOKMARK -> {

                storyViewModel.removeBookmarkFromStory(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    story.id
                )
            }
            StoryOption.LIKE -> {

                storyViewModel.giveLikeToStory(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    story.id
                )
            }
            StoryOption.REMOVE_LIKE -> {
                storyViewModel.removeLikeFromStory(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    story.id
                )
            }
            StoryOption.DOWNLOAD ->
                storyViewModel.downloadStory(
                    story
                )

            StoryOption.REMOVE_DOWNLOAD -> {
                storyViewModel.removeDownloadedStory(
                    story.id
                )
            }
            StoryOption.DIRECTIONS -> openLocationInMapsApp(
                activity, story.lat, story.lng
            )
            StoryOption.SHARE -> {
                shareStory(
                    context, story.id
                )
            }
            else -> Log.d(
                "AuthorFragment", "no option available"
            )
        }
    }
}

