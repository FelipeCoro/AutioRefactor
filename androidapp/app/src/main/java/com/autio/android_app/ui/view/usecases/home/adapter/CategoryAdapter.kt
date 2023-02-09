package com.autio.android_app.ui.view.usecases.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.data.model.story.Category
import com.autio.android_app.databinding.ItemInterestBinding

class CategoryAdapter :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val diffCallback =
        DiffCallback()
    val differ =
        AsyncListDiffer(
            this,
            diffCallback
        )

    inner class CategoryViewHolder(
        private val itemViewBinding: ItemInterestBinding
    ) : RecyclerView.ViewHolder(
        itemViewBinding.root
    ) {
        fun render(
            model: Category
        ) {
            itemViewBinding.tvInterest.text =
                model.title
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val inflater =
            ItemInterestBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        return CategoryViewHolder(
            inflater
        )
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        val item =
            differ.currentList[position]
        holder.render(
            item
        )
    }

    override fun getItemCount() =
        differ.currentList.size

    fun moveItem(
        from: Int,
        to: Int
    ) {
        val list =
            differ.currentList.toMutableList()
        val fromLocation =
            list[from]
        list.removeAt(
            from
        )
        if (to < from) {
            list.add(
                to + 1,
                fromLocation
            )
        } else {
            list.add(
                to - 1,
                fromLocation
            )
        }
        differ.submitList(
            list
        )
    }
}

class DiffCallback :
    DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Category,
        newItem: Category
    ): Boolean {
        return oldItem == newItem
    }
}