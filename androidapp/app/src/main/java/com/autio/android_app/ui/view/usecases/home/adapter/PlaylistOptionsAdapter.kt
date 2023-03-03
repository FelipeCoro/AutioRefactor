package com.autio.android_app.ui.view.usecases.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.PlaylistOptionClickListener
import com.autio.android_app.databinding.ItemListPopupWindowBinding

class PlaylistOptionsAdapter(
    private var options: List<com.autio.android_app.data.api.model.PlaylistOption>,
    private val onOptionClickListener: com.autio.android_app.data.api.model.PlaylistOptionClickListener? = null
) : RecyclerView.Adapter<PlaylistOptionsAdapter.OptionViewHolder>() {

    inner class OptionViewHolder(
        val binding: ItemListPopupWindowBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun render(
            model: com.autio.android_app.data.api.model.PlaylistOption
        ) {
            binding.root.setOnClickListener {
                onOptionClickListener?.onOptionClick(
                    model
                )
            }
            binding.ivOptionIcon.setImageResource(
                model.option.resourceId
            )
            binding.tvOptionTitle.text =
                model.option.title
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        OptionViewHolder(
            ItemListPopupWindowBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: PlaylistOptionsAdapter.OptionViewHolder,
        position: Int
    ) {
        val option =
            options[position]
        holder.render(
            option
        )
    }

    override fun getItemCount() =
        options.size
}
