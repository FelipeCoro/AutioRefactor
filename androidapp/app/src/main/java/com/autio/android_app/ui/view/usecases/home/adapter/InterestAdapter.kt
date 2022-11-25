package com.autio.android_app.ui.view.usecases.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.interest.InterestModel
import com.autio.android_app.ui.view.usecases.home.viewholder.InterestViewHolder

class InterestAdapter(
    private val interestList: List<InterestModel>
) : RecyclerView.Adapter<InterestViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InterestViewHolder {
        val layoutInflater =
            LayoutInflater.from(
                parent.context
            )
        return InterestViewHolder(
            layoutInflater.inflate(
                R.layout.item_interest,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: InterestViewHolder,
        position: Int
    ) {
        val item =
            interestList[position]
        holder.render(
            item
        )
    }

    override fun getItemCount(): Int =
        interestList.size
}