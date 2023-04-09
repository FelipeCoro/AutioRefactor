package com.autio.android_app.ui.subscribe.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentSubscribeBinding
import com.autio.android_app.databinding.MothPopupBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.stories.models.User
import com.autio.android_app.ui.subscribe.adapters.SliderAdapter
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.ui.subscribe.view_states.PurchaseViewState
import com.autio.android_app.util.bottomNavigationActivity
import com.autio.android_app.util.navController
import com.autio.android_app.util.openUrl
import com.autio.android_app.util.resources.DeepLinkingActions
import com.autio.android_app.util.resources.getDeepLinkingNavigationRequest
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getOfferingsWith
import com.smarteist.autoimageslider.SliderView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubscribeFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private lateinit var binding: FragmentSubscribeBinding
    private lateinit var currentUser: User
    private lateinit var yearlySub: Package
    private lateinit var singleTripSub: Package
    private lateinit var threeYearSub: Package


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subscribe, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Purchases.sharedInstance.getOfferingsWith({ error ->
            // An error occurred
        }) { offerings ->
            offerings.current?.availablePackages?.takeUnless { it.isEmpty() }?.let {
                yearlySub = offerings["standard"]?.availablePackages?.get(0)!!
                singleTripSub = offerings["standard"]?.availablePackages?.get(1)!!
                threeYearSub = offerings["standard"]?.availablePackages?.get(2)!!
            }
        }

        bindObservers()
        bindListeners()
        initView()
    }

    private fun bindObservers() {
        //TODO("Bind Observers for state view on purchase flows")
        purchaseViewModel.viewState.observe(viewLifecycleOwner, ::handlePurchaseViewState)

    }

    private fun initView() {
        purchaseViewModel.getUserInfo()
        binding.tvMothInvitation.movementMethod = LinkMovementMethod.getInstance()

        val images = listOf(
            R.drawable.photo_slider1,
            R.drawable.photo_slider2,
            R.drawable.photo_slider3,
            R.drawable.photo_slider4
        )

        val textListTitle = resources.getStringArray(R.array.subscribe_slider_titles).toList()
        val textList = resources.getStringArray(R.array.subscribe_slider_texts).toList()
        val adapter = SliderAdapter(textListTitle, textList, images)

        binding.slider.imageSliderSubscribe.apply {
            autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
            setSliderAdapter(adapter)
            scrollTimeInSec = 6
            isAutoCycle = true
            setInfiniteAdapterEnabled(true)
            startAutoCycle()
        }

        val learnMoreTxt = getString(R.string.subscribe_fragment_learn_more_link)
        binding.tvMothInvitation.makeLinks(learnMoreTxt to { v ->
            learnMoreLinkClicked(v)
        })


    }

    private fun handlePurchaseViewState(viewState: PurchaseViewState?) {
        when (viewState) {
            is PurchaseViewState.FetchedUserSuccess -> handleFetchUserSuccessViewState(viewState.data)
            is PurchaseViewState.UserNotLoggedIn -> handleUserNotLoggedIn()
            is PurchaseViewState.SuccessfulPurchase -> handleSuccessfulPurchase()
            is PurchaseViewState.PurchaseError -> handlePurchaseError(viewState.error)
            is PurchaseViewState.CancelledPurchase -> handleCancelledPurchase()
            else -> {}
        }
    }

    private fun learnMoreLinkClicked(view: View) {
        //TODO(bad practice, refactor implement fragment dialog)
        val dialogBinding = MothPopupBinding.inflate(layoutInflater)
        val dialog = Dialog(view.context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialogBinding.btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnLearnMore.setOnClickListener {
            goToMothCommunityUrlLink()
        }
        dialog.show()
    }

    private fun goToMothCommunityUrlLink() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.subscribe_fragment_url_moth_community_link))
        startActivity(intent)
    }

    private fun bindListeners() {
        purchaseViewModel.customerInfo.observe(viewLifecycleOwner) {
            // updateSubscriptionUI(it)
        }

        binding.slider.lyYoutubeLink.root.setOnClickListener {
            context?.let {
                openUrl(it, getString(R.string.youtube_using_autio_app_video_url))
            }
        }


        if (::yearlySub.isInitialized && ::singleTripSub.isInitialized && ::threeYearSub.isInitialized) {
            binding.cvTraveler.setOnClickListener {
                makePurchase(yearlySub)
            }

            binding.subscriptionPathUi.cvSingleTrip.setOnClickListener {
                makePurchase(singleTripSub)
            }

            binding.subscriptionPathUi.cvAdventurer.setOnClickListener {
                makePurchase(threeYearSub)
            }
        }

        binding.commentSection.btnChoosePlan.setOnClickListener { scrollToSubscribeSection() }

        binding.subscriptionPathUi.btnStoriesFree1.setOnClickListener { travelToMap() }

        binding.commentSection.btnStoriesFree2.setOnClickListener { travelToMap() }
    }

    private fun scrollToSubscribeSection() {
        binding.nestedScroll.post {
            binding.nestedScroll.scrollTo(
                binding.cvTraveler.scrollX, binding.cvTraveler.scrollY
            )
        }
    }


    /*  private fun updateSubscriptionUI(customerInfo: CustomerInfo?) {
          val isUserLogged = currentUser.apiToken.isNotEmpty()
          val isSubscriptionActive =
              customerInfo?.entitlements?.get(REVENUE_CAT_ENTITLEMENT)?.isActive == true

          with(binding.slider.signInOrRestore) {
              if (!isUserLogged) {
                  // Sign in or restore purchase
                  text = resources.getText(R.string.sign_in_or_restore_purchase)
                  makeLinks(
                      context.getString(R.string.subscribe_fragment_sign_in_link) to { goToLoginActivity() },
                      context.getString(R.string.subscribe_fragment_restore_purchase_link) to { purchaseViewModel.restorePurchase() },
                      shouldUnderline = false
                  )

              } else if (!isSubscriptionActive) {
                  // Restore purchase
                  text = resources.getText(R.string.restore_purchase)
                  setOnClickListener { purchaseViewModel.restorePurchase() }
              } else {
                  visibility = View.GONE
              }
          }
      }*/

    private fun makePurchase(mPackage: Package) {
        activity?.let {
            purchaseViewModel.purchasePackage(it, mPackage)
        }
    }

    private fun travelToMap() {
        navController.navigate(R.id.action_subscribeFragment_to_map_fragment)
    }

    private fun isSessionAlive() {
        //  if (isSessionAlive.isEmpty()) {
        //      goToLoginActivity()
        //  } else {


    }

    private fun handleUserNotLoggedIn() {
        Toast.makeText(
            context,
            "Please create or log into an account before making a purchase",
            Toast.LENGTH_LONG
        ).show()
        navController.navigate(R.id.action_subscribeFragment_to_authentication_nav)
        bottomNavigationActivity?.finish()
    }

    private fun handleFetchUserSuccessViewState(user: User) {
        if (user.remainingStories <= 0) {
            binding.subscriptionPathUi.btnStoriesFree1.visibility =View.INVISIBLE
            binding.commentSection.btnStoriesFree2.visibility =View.GONE
        }
    }

    private fun handleSuccessfulPurchase() {
        Toast.makeText(
            context,
            "Purchase successful — Welcome to the Autio community.",
            Toast.LENGTH_LONG
        ).show()
        navController.navigate(R.id.action_subscribeFragment_to_map_fragment)
    }

    private fun handlePurchaseError(errorMessage: String) {
        Toast.makeText(
            context,
            errorMessage,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCancelledPurchase() {
        Toast.makeText(
            context,
            "Your purchase was not made",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun goToLoginActivity() {
        val request =
            getDeepLinkingNavigationRequest(DeepLinkingActions.LoginFragmentDeepLinkingAction)
        val nav = findNavController()
        nav.navigate(request)
    }
}
