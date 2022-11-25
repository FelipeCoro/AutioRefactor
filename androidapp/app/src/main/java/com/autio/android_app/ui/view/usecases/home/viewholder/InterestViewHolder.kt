package com.autio.android_app.ui.view.usecases.home.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.data.model.interest.InterestModel
import com.autio.android_app.databinding.ItemInterestBinding

class InterestViewHolder(
    view: View
) : RecyclerView.ViewHolder(
    view
) {

    val binding =
        ItemInterestBinding.bind(
            view
        )

    fun render(
        interestModel: InterestModel
    ) {
        binding.tvInterest.text =
            interestModel.interest
    }

}