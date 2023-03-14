package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentAccountBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.stories.adapter.CategoryAdapter
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.ui.viewmodel.AccountFragmentViewModel
import com.autio.android_app.util.Constants.REVENUE_CAT_ENTITLEMENT
import com.autio.android_app.util.checkEmptyField
import com.autio.android_app.util.openUrl
import com.autio.android_app.util.pleaseFillText
import com.autio.android_app.util.showError
import com.autio.android_app.util.showPaywall
import com.autio.android_app.util.showToast
import com.autio.android_app.util.writeEmailToCustomerSupport
import com.bumptech.glide.Glide
import com.revenuecat.purchases.CustomerInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class AccountFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient:ApiClient

    private val accountFragmentViewModel: AccountFragmentViewModel by viewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private val purchaseViewModel: PurchaseViewModel by viewModels()

    private lateinit var binding: FragmentAccountBinding

    private var name = ""
    private var email = ""
    private val originalCategories = arrayListOf<Category>()
    private val tempCategories = arrayListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindListeners()
        setListeners()
        initView()
    }

    private fun initView() {
        accountFragmentViewModel.fetchUserData()
        prepareView()
        initRecyclerViewInterest()
    }

    private fun setListeners() {
        // Updates UI based on subscription status
        purchaseViewModel.customerInfo.observe(viewLifecycleOwner) {
            it?.let { updateSubscriptionUI(it) }
        }

        // Listeners for buttons for subscription's buttons (status, gift, discount)
        setSubscriptionRelatedListeners()

        // Listeners for personal information text fields
        setProfileListeners()

        // Listeners for password form's buttons and text fields
        setPasswordFormListeners()

        // Categories order
        binding.btnSaveCategoriesChanges.setOnClickListener {
            updateCategories()
        }

        // Youtube links and other media listeners
        setMediaLinksListeners()

        // Write email to Autio's support team
        setCustomerSupportListeners()

        binding.btnDeleteAccount.setOnClickListener {
//            apiService.deleteAccount() /TODO()
        }
    }

    private fun setSubscriptionRelatedListeners() {
        with(binding) {
            btnGift.setOnClickListener {
                openUrl(requireContext(), "https://app.autio.com/give")
            }

            tvManageSubscription.setOnClickListener {
                if (activity != null) {
                    showPaywall(requireActivity())
                }
            }

            btnSendDiscount.setOnClickListener {
                // TODO: Create discount code from Google Play
            }
        }
    }

    private fun setProfileListeners() {
        with(binding) {
            etName.doAfterTextChanged {
                checkIfDataChanged()
            }

            etEmail.doAfterTextChanged {
                checkIfDataChanged()
            }

            btnUpdateProfile.setOnClickListener {
                updateUserData()
            }

            btnCancelUpdate.setOnClickListener {
                llUpdateProfileButtons.visibility = GONE
                getUserInfo()
            }
        }
    }

    private fun checkIfDataChanged() {
        with(binding) {
            llUpdateProfileButtons.visibility =
                if (etName.text.toString() != name || etEmail.text.toString() != email) VISIBLE else GONE
        }
    }

    private fun setPasswordFormListeners() {
        with(binding) {
            btnChangePassword.setOnClickListener {
                llChangePasswordForm.visibility = VISIBLE
                llChangePasswordButtons.visibility = VISIBLE
                btnChangePassword.visibility = GONE
            }

            btnCancelPassword.setOnClickListener {
                llChangePasswordForm.visibility = GONE
                llChangePasswordButtons.visibility = GONE
                btnChangePassword.visibility = VISIBLE
            }

            btnUpdatePassword.setOnClickListener {
                changeUserPassword()
            }
        }
    }

    private fun setMediaLinksListeners() {
        with(binding) {
            lyYoutubeLink.root.setOnClickListener {
                openUrl(
                    requireContext(), "https://www.youtube.com/watch?v=SvbahDL4aYc&ab_channel=Autio"
                )
            }

            lyYoutubeGuest.root.setOnClickListener {
                openUrl(
                    requireContext(), "https://www.youtube.com/watch?v=SvbahDL4aYc&ab_channel=Autio"
                )
            }
        }
    }

    private fun setCustomerSupportListeners() {
        with(binding) {
            btnContact.setOnClickListener {
                writeEmailToCustomerSupport(
                    requireContext()
                )
            }
            tvQuestionsAbout.makeLinks("Contact Autio" to { v ->
                writeEmailToCustomerSupport(requireContext())
            })

            tvContactSupport.makeLinks("Contact Autio" to { v ->
                writeEmailToCustomerSupport(requireContext())
            })
        }
    }
    private fun updateUserData() {
        if (checkEmptyField(binding.etName) || checkEmptyField(binding.etEmail)) {
            pleaseFillText(requireContext())
        } else {
            val name = "${binding.etName.text}"
            val email = "${binding.etEmail.text}"
            accountFragmentViewModel.updateProfile(name, email, originalCategories, onSuccess = {
                binding.llUpdateProfileButtons.visibility = GONE
                showToast(requireContext(), "Profile has been updated")
            }, onFailure = {
                showError(requireContext())
            })
        }
    }

    private fun updateSubscriptionUI(customerInfo: CustomerInfo) {
        with(binding) {
            if (customerInfo.entitlements[REVENUE_CAT_ENTITLEMENT]?.isActive == true) {
                tvPlanStatus.text = resources.getText(R.string.status_subscribed)
                tvRestorePurchase.visibility = GONE
            } else {
                tvPlanStatus.text = resources.getText(R.string.no_plan_selected)
                tvRestorePurchase.apply {
                    setOnClickListener {
                        purchaseViewModel.restorePurchase()
                    }
                    visibility = VISIBLE
                }
            }
        }
    }

    private fun changeUserPassword() {
        if (checkEmptyField(binding.etCurrentPassword) || checkEmptyField(binding.etNewPassword) || checkEmptyField(
                binding.etConfirmPassword
            )
        ) {
            pleaseFillText(requireContext())
        } else {
            val currentPassword = binding.etCurrentPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val passwordInfo = com.autio.android_app.data.api.model.account.ChangePasswordDto(
                currentPassword, newPassword, confirmPassword
            )
            if (newPassword == confirmPassword) {
                //TODO (move to VM -> repo)
                lifecycleScope.launch {
                    val result = apiClient.changePassword(
                        prefRepository.userId, prefRepository.userApiToken, passwordInfo
                    )
                    if (result.isSuccessful) {
                        binding.llChangePasswordForm.visibility = GONE
                        binding.llChangePasswordButtons.visibility = GONE
                        binding.btnChangePassword.visibility = VISIBLE
                        showToast(requireContext(), "Password has been updated")
                    }
                }
            }
        }
    }

    private fun updateCategories() {
        // This was implemented as this because of a bug in backend
        // where order in which data was passed actually mattered
        // TODO: Update to more optimal code after bug fix reported from backend
        accountFragmentViewModel.saveCategoriesOrder(tempCategories.mapIndexed { i, cat ->
            Category(id = cat.id, order = i + 1, title = cat.title)
        }.sortedBy { it.id }, onSuccess = {
            showToast(requireContext(), "Profile has been updated")
        }, onFailure = {
            showError(requireContext())
        })
    }

    private fun getUserInfo() {
        name = prefRepository.userName.trim()
        email = prefRepository.userEmail
        val email = prefRepository.userEmail
        binding.etName.setText(name)
        binding.etEmail.setText(email)
    }

    private fun prepareView() {
        Glide.with(binding.ivAccount).load(R.drawable.account_header).fitCenter()
            .into(binding.ivAccount)

        val isGuest = prefRepository.isUserGuest
        binding.scrollViewAccount.isGone = isGuest
        binding.linearLayoutSignIn.isVisible = isGuest

        if (!isGuest) {
            getUserInfo()
        }
    }

    private fun bindListeners() {
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
        binding.rvInterests.layoutManager = LinearLayoutManager(context)
        storyViewModel.userCategories.observe(viewLifecycleOwner) { roomCategories ->
            if (originalCategories != roomCategories) {
                binding.btnSaveCategoriesChanges.visibility = GONE
                originalCategories.clear()
                originalCategories.addAll(roomCategories)
                tempCategories.clear()
                tempCategories.addAll(originalCategories)
                val adapter = CategoryAdapter()
                adapter.differ.submitList(originalCategories)
                binding.rvInterests.adapter = adapter
                itemTouchHelper.attachToRecyclerView(binding.rvInterests)
            }
        }
    }

    private fun logOut() {
        // Cleans stories downloaded data from device
        val audioDir = File(requireContext().filesDir, "audio")
        val imagesDir = File(requireContext().filesDir, "images")
        if (audioDir.exists()) {
            audioDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }
        if (imagesDir.exists()) {
            imagesDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }
        storyViewModel.clearUserData()

        purchaseViewModel.logOut()

        // Clears shared preferences user's data
        prefRepository.clearData()
        gotoLoginFragment()
    }

    private fun gotoLoginFragment() {

    //TODO(This is not working)
    val request =
         NavDeepLinkRequest.Builder
             .fromUri("android-app://navigation.autio.app/login".toUri())
             .build()
     val nav = findNavController()
     nav.navigate(request)
    }

    private fun goToSignIn() {
        val request =
            NavDeepLinkRequest.Builder.fromUri("android-app://navigation.autio.app/sign-in".toUri())
                .build()
        val nav = findNavController()
        nav.navigate(request)
    }

    private fun goToSignUp() {
        val request =
            NavDeepLinkRequest.Builder.fromUri("android-app://navigation.autio.app/login".toUri())
                .build()
        val nav = findNavController()
        nav.navigate(request)
    }

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : SimpleCallback(UP or DOWN or START or END, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter as CategoryAdapter
                val from = viewHolder.absoluteAdapterPosition
                val to = target.absoluteAdapterPosition
                adapter.moveItem(from, to)
                adapter.notifyItemMoved(from, to)

                val category = tempCategories[from]
                tempCategories.remove(category)
                tempCategories.add(to, category)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                binding.btnSaveCategoriesChanges.isGone = originalCategories == tempCategories
                viewHolder.itemView.alpha = 1.0f
            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }


}
