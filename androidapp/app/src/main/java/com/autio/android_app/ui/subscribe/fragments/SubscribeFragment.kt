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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentSubscribeBinding
import com.autio.android_app.databinding.MothPopupBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.subscribe.adapters.SliderAdapter
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.util.Constants.REVENUE_CAT_ENTITLEMENT
import com.autio.android_app.util.openUrl
import com.autio.android_app.util.resources.DeepLinkingActions
import com.autio.android_app.util.resources.getDeepLinkingNavigationRequest
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.smarteist.autoimageslider.SliderView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubscribeFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private lateinit var binding: FragmentSubscribeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subscribe, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindObservers()
        bindListeners()
        initView()
    }

    private fun bindObservers() {
        //TODO("Bind Observers for state view on purchase flows")
    }

    private fun initView() {
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

        purchaseViewModel.getOfferings()
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
            updateSubscriptionUI(it)
        }

        binding.slider.lyYoutubeLink.root.setOnClickListener {
            context?.let {
                openUrl(it, getString(R.string.youtube_using_autio_app_video_url))
            }
        }


        /*{ offerings -> //TODO (Make purchase handle viewStates for offerings)
            offerings.current?.availablePackages?.takeUnless { it.isEmpty() }?.let { packages ->
                with(binding) {
                    packages.forEach { p ->
                        when (p.product.sku) {
                            SINGLE_TRIP_PRODUCT -> {
                                subscriptionPathUi.cvSingleTrip.setOnClickListener { makePurchase(p) }
                            }
                            ADVENTURER_TRIP_PRODUCT -> {
                                subscriptionPathUi.cvAdventurer.setOnClickListener { makePurchase(p) }
                            }
                            TRAVELER_TRIP_SUBSCRIPTION -> {
                                cvTraveler.setOnClickListener { makePurchase(p) }
                            }
                        }
                    }
                }
            }
        }
*/

        binding.commentSection.btnChoosePlan.setOnClickListener {
            scrollToSubscribeSection()
        }

        binding.subscriptionPathUi.btnStoriesFree1.setOnClickListener {
            isSessionAlive()
        }

        binding.commentSection.btnStoriesFree2.setOnClickListener {
            isSessionAlive()
        }
    }

    private fun scrollToSubscribeSection() {
        binding.nestedScroll.post {
            binding.nestedScroll.scrollTo(
                binding.cvTraveler.scrollX, binding.cvTraveler.scrollY
            )
        }
    }

    private fun updateSubscriptionUI(customerInfo: CustomerInfo?) {
        val isUserLogged = prefRepository.userApiToken.isNotEmpty()
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
    }

    private fun makePurchase(mPackage: Package) {
        activity?.let {

            /*
            purchaseViewModel.purchasePackage(
                it,
                mPackage,
                onError = { _, _ -> }, { _, _ ->
                    // Check if paywall was invoked from a logged in account (either verified
                    // or guest user) so it pops to the main screen once the purchase is done
                    if (intent.getStringExtra("ACTIVITY_NAME") == BottomNavigation::class.simpleName) {
                        // Go to account page
                        it.finish()
                    }
                }
                */ //TODO (Make purchase handle viewStates)
        }
    }

    private fun isSessionAlive() {
        val isSessionAlive = prefRepository.userApiToken
        if (isSessionAlive.isEmpty()) {
            goToLoginActivity()
        } else {
            getToMainMenu()
        }
    }

    private fun getToMainMenu() {
        val intent = Intent(context, BottomNavigation::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        startActivity(intent)
    }

    private fun goToLoginActivity() {
        val request =
            getDeepLinkingNavigationRequest(DeepLinkingActions.LoginFragmentDeepLinkingAction)
        val nav = findNavController()
        nav.navigate(request)
    }
}
