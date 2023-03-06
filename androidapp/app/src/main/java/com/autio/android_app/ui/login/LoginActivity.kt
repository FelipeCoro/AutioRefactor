package com.autio.android_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.Datasource
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.ActivityLoginBinding
import com.autio.android_app.extensions.setAutomaticScroll
import com.autio.android_app.ui.stories.adapter.ImageAdapter
import com.autio.android_app.util.showError
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firstRecyclerView: RecyclerView
    private lateinit var secondRecyclerView: RecyclerView
    private lateinit var thirdRecyclerView: RecyclerView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding = ActivityLoginBinding.inflate(
            layoutInflater
        )
        setContentView(
            binding.root
        )

        setUpBackgroundAnimation()
        setListeners()
    }

    private fun setUpBackgroundAnimation() {
        val imageDataset = Datasource().loadLocationViews()
        firstRecyclerView = findViewById(
            R.id.rvFirstColumn
        )
        secondRecyclerView = findViewById(
            R.id.rvSecondColumn
        )
        thirdRecyclerView = findViewById(
            R.id.rvThirdColumn
        )

        firstRecyclerView.adapter = ImageAdapter(
            imageDataset.shuffled()
        )
        firstRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        secondRecyclerView.adapter = ImageAdapter(
            imageDataset.shuffled()
        )
        secondRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        thirdRecyclerView.adapter = ImageAdapter(
            imageDataset.shuffled()
        )
        thirdRecyclerView.layoutManager!!.scrollToPosition(
            Integer.MAX_VALUE / 2
        )

        firstRecyclerView.setAutomaticScroll()
        secondRecyclerView.setAutomaticScroll(
            ScrollView.FOCUS_UP
        )
        thirdRecyclerView.setAutomaticScroll()
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener {
            startActivity(
                Intent(
                    this, SignInActivity::class.java
                )
            )
        }
        binding.btnSignup.setOnClickListener {
            startActivity(
                Intent(
                    this, SignUpActivity::class.java
                )
            )
        }
        binding.btnLoginAsGuest.setOnClickListener {
            loginGuest()
        }
    }

    private fun showLoadingView() {
        binding.flLoading.root.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility = View.GONE
    }

    private fun loginGuest() {
        showLoadingView()
        //TODO(Had to comment out to run)
        /*    apiClient.loginAsGuest() {
                if (it != null) {
                    saveGuestInfo(it)
                    startActivity(
                        Intent(
                            this,
                            com.autio.android_app.ui.stories.BottomNavigation::class.java
                        )
                    ) finish ()
                } else {
                    hideLoadingView() showError (this)
                }
            }*/
    }

    private fun saveGuestInfo(
        guestResponse: com.autio.android_app.data.api.model.account.GuestResponse
    ) {
        prefRepository.isUserGuest = true
        prefRepository.userId = guestResponse.id
        prefRepository.firebaseKey = guestResponse.firebaseKey
        prefRepository.userApiToken = guestResponse.apiToken
        prefRepository.remainingStories = 5
    }
}
