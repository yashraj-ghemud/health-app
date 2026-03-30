package com.mindguard.ui.settings.categories.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.AppCategoryMapping

class AppCategoriesAdapter(
    private val onCategoryChanged: (AppCategoryMapping, AppCategory) -> Unit
) : ListAdapter<AppCategoryMapping, AppCategoriesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind logic
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<AppCategoryMapping>() {
        override fun areItemsTheSame(oldItem: AppCategoryMapping, newItem: AppCategoryMapping): Boolean = oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppCategoryMapping, newItem: AppCategoryMapping): Boolean = oldItem == newItem
    }
}
