package com.autio.android_app.ui.subscribe

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.ActivitySubscribeBinding
import com.autio.android_app.databinding.MothPopupBinding
import com.autio.android_app.extensions.makeLinks
import com.autio.android_app.ui.login.LoginActivity
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.util.Constants.ADVENTURER_TRIP_PRODUCT
import com.autio.android_app.util.Constants.REVENUE_CAT_ENTITLEMENT
import com.autio.android_app.util.Constants.SINGLE_TRIP_PRODUCT
import com.autio.android_app.util.Constants.TRAVELER_TRIP_SUBSCRIPTION
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.openUrl
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.smarteist.autoimageslider.SliderView
import dagger.hilt.EntryPoint
import javax.inject.Inject

@EntryPoint
class SubscribeActivity : AppCompatActivity() {

    @Inject
    lateinit var prefRepository: PrefRepository

    private val purchaseViewModel by viewModels<PurchaseViewModel> {
        InjectorUtils.providePurchaseViewModel(
            this
        )
    }

    private lateinit var binding: ActivitySubscribeBinding

    private val textListTitle = listOf(
        "Take your journey to the next level with location-based audio stories",
        "Go anywhere with nationwide coverage",
        "Always more to discover with new original content",
        "A collection of narrators as unique as the stories"
    )
    private val textList = listOf(
        "Explore our collection of over 10,000+ stories exclusive to Autio.",
        "Road trip across the country with new stories to discover wherever you travel.",
        "New, unique stories added weekly.",
        "Listen to some of your favorite voices, like Kevin Costner, Phil Jackson, & John Lithgow."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe)

        binding.tvMothInvitation.movementMethod = LinkMovementMethod.getInstance()

        val adapter = SliderAdapter(textListTitle, textList)

        binding.imageSliderSubscribe.apply {
            autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
            setSliderAdapter(adapter)
            scrollTimeInSec = 6
            isAutoCycle = true
            setInfiniteAdapterEnabled(true)
            startAutoCycle()
        }

        setListeners()
    }

    private fun setListeners() {
        purchaseViewModel.customerInfo.observe(this) {
            updateSubscriptionUI(it)
        }

        binding.lyYoutubeLink.root.setOnClickListener {
            openUrl(this, "https://www.youtube.com/watch?v=SvbahDL4aYc&ab_channel=Autio")
        }

        purchaseViewModel.getOfferings { offerings ->
            offerings.current?.availablePackages?.takeUnless { it.isEmpty() }?.let { packages ->
                with(binding) {
                    packages.forEach { p ->
                        when (p.product.sku) {
                            SINGLE_TRIP_PRODUCT -> {
                                cvSingleTrip.setOnClickListener { makePurchase(p) }
                            }
                            ADVENTURER_TRIP_PRODUCT -> {
                                cvAdventurer.setOnClickListener { makePurchase(p) }
                            }
                            TRAVELER_TRIP_SUBSCRIPTION -> {
                                cvTraveler.setOnClickListener { makePurchase(p) }
                            }
                        }
                    }
                }
            }
        }

        binding.tvMothInvitation.makeLinks("Learn more" to View.OnClickListener {
            val dialogBinding = MothPopupBinding.inflate(layoutInflater)
            val dialog = Dialog(this)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(dialogBinding.root)
            dialogBinding.btnBack.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.btnLearnMore.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://themoth.org/community")
                startActivity(intent)
            }
            dialog.show()
        })

        binding.btnChoosePlan.setOnClickListener {
            binding.scrollView.smoothScrollTo(0, binding.cvTraveler.scrollY)
        }

        binding.btnStoriesFree1.setOnClickListener {
            isSessionAlive()
        }

        binding.btnStoriesFree2.setOnClickListener {
            isSessionAlive()
        }
    }

    private fun updateSubscriptionUI(customerInfo: CustomerInfo?) {
        val isUserLogged =
            prefRepository.userApiToken.isNotEmpty() && prefRepository.firebaseKey.isNotEmpty()
        val isSubscriptionActive =
            customerInfo?.entitlements?.get(REVENUE_CAT_ENTITLEMENT)?.isActive == true

        with(binding.tvSignInOrRestorePurchase) {
            if (!isUserLogged) {
                // Sign in or restore purchase
                text = resources.getText(R.string.sign_in_or_restore_purchase)
                makeLinks("Sign In" to View.OnClickListener {
                    goToLoginActivity()
                }, "Restore Purchase" to View.OnClickListener {
                    purchaseViewModel.restorePurchase()
                }, shouldUnderline = false
                )

            } else if (!isSubscriptionActive) {
                // Restore purchase
                text = resources.getText(
                    R.string.restore_purchase
                )
                setOnClickListener { purchaseViewModel.restorePurchase() }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun makePurchase(
        mPackage: Package
    ) {
        purchaseViewModel.purchasePackage(
            this, mPackage, onError = { _, _ -> },
        ) { _, _ ->
            // Check if paywall was invoked from a logged in account (either verified
            // or guest user) so it pops to the main screen once the purchase is done
            if (intent.getStringExtra(
                    "ACTIVITY_NAME"
                ) == com.autio.android_app.ui.stories.BottomNavigation::class.simpleName
            ) {
                // Go to account page
                finish()
            }
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
        val intent = Intent(this, BottomNavigation::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        startActivity(intent)
        finish()
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
