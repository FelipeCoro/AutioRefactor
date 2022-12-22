package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.account.ChangePasswordDto
import com.autio.android_app.data.model.account.UpdateProfileDto
import com.autio.android_app.data.model.interest.InterestProvider
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentAccountBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.view.usecases.home.adapter.InterestAdapter
import com.autio.android_app.ui.view.usecases.login.LoginActivity
import com.autio.android_app.ui.view.usecases.login.SignInActivity
import com.autio.android_app.ui.view.usecases.login.SignUpActivity
import com.autio.android_app.util.SwipeGesture
import com.autio.android_app.util.Utils
import com.bumptech.glide.Glide
import java.util.*

class AccountFragment :
    Fragment() {

    private var _binding: FragmentAccountBinding? =
        null
    private val binding get() = _binding!!
    private val interestList =
        InterestProvider.getInterests()
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    private val apiService =
        ApiService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentAccountBinding.inflate(
                inflater,
                container,
                false
            )
        prepareView()

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        intentFunctions()
        initRecyclerViewInterest()
        setListeners()
    }

    private fun setListeners() {
        binding.etName.doOnTextChanged { text, _, _, _ ->
            run {
                if (text?.toString()
                        ?.equals(
                            prefRepository.userName
                        ) == false
                ) binding.lyUpdateProfile.visibility =
                    VISIBLE
            }
        }

        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            run {
                if (text?.toString()
                        ?.equals(
                            prefRepository.userEmail
                        ) == false
                ) binding.lyUpdateProfile.visibility =
                    VISIBLE
            }
        }

        binding.btnChangePassword.setOnClickListener {
            binding.lyChangePassword.visibility =
                VISIBLE
            binding.lyBtnsChangePassword.visibility =
                VISIBLE
            binding.btnChangePassword.visibility =
                GONE
        }

        binding.btnCancelPassword.setOnClickListener {
            binding.lyChangePassword.visibility =
                GONE
            binding.lyBtnsChangePassword.visibility =
                GONE
            binding.btnChangePassword.visibility =
                VISIBLE
        }

        binding.btnUpdatePassword.setOnClickListener {
            changeUserPassword()
        }

        binding.btnCancelUpdate.setOnClickListener {
            binding.lyUpdateProfile.visibility =
                GONE
            getUserInfo()
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateUserData()
        }

        binding.rlWatchHowWorks.setOnClickListener {
            Utils.openUrl(
                requireContext(),
                "https://www.youtube.com/watch?v=SvbahDL4aYc&ab_channel=Autio"
            )
        }

        binding.btnContact.setOnClickListener {
            Utils.writeEmail(
                requireContext(),
                arrayOf(
                    "support@autio.com"
                ),
                "Autio Android Customer Support",
                """
                    Device: ${Utils.getDeviceData()}
                    Android Version: ${android.os.Build.VERSION.SDK_INT}
                    App Version: ${android.os.Build.VERSION.RELEASE}
                """.trimIndent()
            )
        }

        binding.tvGuestContactAutio.makeLinks(
            Pair(
                "Contact Autio",
                View.OnClickListener {
                    Utils.writeEmail(
                        requireContext(),
                        arrayOf(
                            "support@autio.com"
                        ),
                        "Autio Android Customer Support",
                        """
                    Device: ${Utils.getDeviceData()}
                    Android Version: ${android.os.Build.VERSION.SDK_INT}
                    App Version: ${android.os.Build.VERSION.RELEASE}
                """.trimIndent()
                    )
                })
        )
    }

    private fun updateUserData() {
        if (Utils.checkEmptyField(
                binding.etName
            ) || Utils.checkEmptyField(
                binding.etEmail
            )
        ) {
            Utils.pleaseFillText(
                requireContext()
            )
        } else {
            val name =
                binding.etName.text.toString()
            val email =
                binding.etEmail.text.toString()
            val infoUser =
                UpdateProfileDto(
                    email,
                    name
                )
            apiService.updateProfile(
                getUserId(),
                getApiToken(),
                getUserId(),
                infoUser
            ) {
                if (it != null) {
                    saveUserInfo(
                        it
                    )
                }
            }
        }
    }

    private fun changeUserPassword() {
        if (Utils.checkEmptyField(
                binding.etCurrentPassword
            ) || Utils.checkEmptyField(
                binding.etNewPassword
            ) || Utils.checkEmptyField(
                binding.etConfirmPassword
            )
        ) {
            Utils.pleaseFillText(
                requireContext()
            )
        } else {
            val currentPassword =
                binding.etCurrentPassword.text.toString()
            val newPassword =
                binding.etNewPassword.text.toString()
            val confirmPassword =
                binding.etConfirmPassword.text.toString()
            val passwordInfo =
                ChangePasswordDto(
                    currentPassword,
                    newPassword,
                    confirmPassword
                )
            if (newPassword == confirmPassword) {
                apiService.changePassword(
                    getUserId(),
                    getApiToken(),
                    passwordInfo
                ) {
                    if (it != null) {
                        binding.lyChangePassword.visibility =
                            GONE
                        binding.lyBtnsChangePassword.visibility =
                            GONE
                        binding.btnChangePassword.visibility =
                            VISIBLE
                        Utils.showToast(
                            requireContext(),
                            "Password has been updated"
                        )
                    }
                }
            }
        }
    }

    private fun getUserInfo() {
        val name =
            prefRepository.userName
        val email =
            prefRepository.userEmail
        binding.etName.setText(
            name
        )
        binding.etEmail.setText(
            email
        )
    }

    private fun prepareView() {
        Glide.with(
            binding.ivAccount
        )
            .load(
                R.drawable.account_image_2
            )
            .fitCenter()
            .into(
                binding.ivAccount
            )

        if (isUserGuest()) {
            binding.scrollViewAccount.visibility =
                GONE
            binding.linearLayoutSignIn.visibility =
                VISIBLE
        } else {
            binding.scrollViewAccount.visibility =
                VISIBLE
            binding.linearLayoutSignIn.visibility =
                GONE
        }

        getUserInfo()
    }

    private fun intentFunctions() {
        binding.btnSignIn.setOnClickListener {
            goToSignIn()
        }
        binding.btnSignUp.setOnClickListener {
            goToSignUp()
        }
        binding.btnLogOut.setOnClickListener {
            logOut()
        }
    }

    private fun initRecyclerViewInterest() {
        val adapter =
            InterestAdapter(
                interestList
            )
        val swipeGesture =
            object :
                SwipeGesture(
                    context
                ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPos =
                        viewHolder.adapterPosition
                    val toPos =
                        target.adapterPosition
                    Collections.swap(
                        interestList,
                        fromPos,
                        toPos
                    )
                    adapter.notifyItemMoved(
                        fromPos,
                        toPos
                    )

                    return false
                }

            }
        val touchHelper =
            ItemTouchHelper(
                swipeGesture
            )
        touchHelper.attachToRecyclerView(
            binding.recyclerViewInterests
        )
        binding.recyclerViewInterests.layoutManager =
            LinearLayoutManager(
                context
            )
        binding.recyclerViewInterests.adapter =
            adapter
    }

    private fun logOut() {
        prefRepository.clearData()
        startActivity(
            Intent(
                activity,
                LoginActivity::class.java
            )
        )
        activity?.finish()
    }

    private fun goToSignIn() {
        val signInIntent =
            Intent(
                activity,
                SignInActivity::class.java
            )
        startActivity(
            signInIntent
        )
    }

    private fun goToSignUp() {
        val signUpIntent =
            Intent(
                activity,
                SignUpActivity::class.java
            )
        startActivity(
            signUpIntent
        )
    }

    private fun isUserGuest(): Boolean =
        prefRepository.isUserGuest

    private fun getUserId(): Int =
        prefRepository.userId

    private fun getApiToken(): String =
        "Bearer " + prefRepository.userApiToken

    private fun saveUserInfo(
        updateProfileDto: UpdateProfileDto
    ) {
        prefRepository.userName =
            updateProfileDto.name
        prefRepository.userEmail =
            updateProfileDto.email
    }
}