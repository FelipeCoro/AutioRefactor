package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.account.ChangePasswordDto
import com.autio.android_app.data.model.Interest.InterestProvider
import com.autio.android_app.data.model.account.UpdateProfileDto
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentAccountBinding
import com.autio.android_app.ui.view.usecases.home.adapter.InterestAdapter
import com.autio.android_app.ui.view.usecases.login.LoginActivity
import com.autio.android_app.ui.view.usecases.login.SignInActivity
import com.autio.android_app.ui.view.usecases.login.SignUpActivity
import com.autio.android_app.util.SwipeGesture
import com.autio.android_app.util.Utils
import com.bumptech.glide.Glide
import java.util.*

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val interestList = InterestProvider.getInterests()
    private val prefRepository by lazy { PrefRepository(requireContext()) }
    private val apiService = ApiService()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        prepareView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intentFunctions()
        initRecyclerViewInterest()
        setListeners()
    }

    private fun setListeners(){

        binding.etName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.lyUpdateProfile.visibility = VISIBLE
            }

            override fun afterTextChanged(p0: Editable?) {
                return
            }

        })

        binding.btnChangePassword.setOnClickListener {
            binding.lyChangePassword.visibility = VISIBLE
            binding.lyBtnsChangePassword.visibility = VISIBLE
            binding.btnChangePassword.visibility = GONE
        }

        binding.btnCancelPassword.setOnClickListener {
            binding.lyChangePassword.visibility = GONE
            binding.lyBtnsChangePassword.visibility = GONE
            binding.btnChangePassword.visibility = VISIBLE
        }

        binding.btnUpdatePassword.setOnClickListener {
            changeUserPassword()
        }

        binding.btnCancelUpdate.setOnClickListener {
            binding.lyUpdateProfile.visibility = GONE
            getUserInfo()
        }
        binding.btnUpdateProfile.setOnClickListener {
            updateUserData()
        }
    }

    private fun updateUserData(){
        if(Utils.checkEmptyField(binding.etName) || Utils.checkEmptyField(binding.etEmail)){
            Utils.pleaseFillText(requireContext())
        }else{
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val infoUser = UpdateProfileDto(email,name)
            apiService.updateProfile(getUserId(),getApiToken(),getUserId(),infoUser){
                if (it != null){
                    saveUserInfo(it)
                }
            }
        }
    }

    private fun changeUserPassword(){
        if(Utils.checkEmptyField(binding.etCurrentPassword) || Utils.checkEmptyField(binding.etNewPassword) || Utils.checkEmptyField(binding.etConfirmPassword)){
            Utils.pleaseFillText(requireContext())
        }else{
            val currentPassword = binding.etCurrentPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassowrd = binding.etConfirmPassword.text.toString()
            val passwordInfo = ChangePasswordDto(currentPassword,newPassword,confirmPassowrd)
            if (newPassword == confirmPassowrd){
                apiService.changePassword(getUserId(),getApiToken(), passwordInfo){
                    if (it!=null){
                        binding.lyChangePassword.visibility = GONE
                        binding.lyBtnsChangePassword.visibility = GONE
                        binding.btnChangePassword.visibility = VISIBLE
                        Utils.showToast(requireContext(),"Password has been updated")
                    }
                }
            }
        }
    }

    private fun getUserInfo(){
        val name = prefRepository.getUserName()
        val email = prefRepository.getUserEmail()
        binding.etName.setText(name)
        binding.etEmail.setText(email)
    }

    private fun prepareView() {
        Glide.with(binding.ivAccount).load(R.drawable.account_image_2).fitCenter()
            .into(binding.ivAccount)

        if(isUserGuest()){
            binding.scrollViewAccount.visibility = GONE
            binding.linearLayoutSignIn.visibility = VISIBLE
        }else{
            binding.scrollViewAccount.visibility = VISIBLE
            binding.linearLayoutSignIn.visibility = GONE
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
        val manager = LinearLayoutManager(context)
        val adapter = InterestAdapter(interestList)
        val swipeGesture = object : SwipeGesture(context) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                Collections.swap(interestList, fromPos, toPos)
                adapter.notifyItemMoved(fromPos, toPos)

                return false
            }

        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.recyclerViewInterests)
        binding.recyclerViewInterests.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewInterests.adapter = adapter
    }

    private fun logOut() {
        prefRepository.clearData()
        startActivity(Intent(activity, LoginActivity::class.java))
        activity?.finish()
    }

    private fun goToSignIn() {
        val signInIntent = Intent(activity, SignInActivity::class.java)
        startActivity(signInIntent)
    }

    private fun goToSignUp() {
        val signUpIntent = Intent(activity, SignUpActivity::class.java)
        startActivity(signUpIntent)
    }

    private fun isUserGuest():Boolean = prefRepository.getIsUserGuest()

    private fun getUserId():Int = prefRepository.getUserId()

    private fun getApiToken(): String = "Bearer "+prefRepository.getUserApiToken()

    private fun saveUserInfo(updateProfileDto: UpdateProfileDto) {
        prefRepository.setUserName(updateProfileDto.name)
        prefRepository.setUserEmail(updateProfileDto.email)
    }
}