package com.mindguard.ui.onboarding.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.AppCategory
import com.mindguard.ui.onboarding.AppReviewFragment.AppCategoryItem

class AppCategoryAdapter(
    private val onCategoryChanged: (String, AppCategory) -> Unit
) : ListAdapter<AppCategoryItem, AppCategoryAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_category, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val categorySpinner: android.widget.AutoCompleteTextView = itemView.findViewById(R.id.categorySpinner)
        
        fun bind(item: AppCategoryItem) {
            appIcon.setImageDrawable(item.icon)
            appName.text = item.appLabel
            
            val categories = AppCategory.values().map { it.displayName }
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, categories)
            categorySpinner.setAdapter(adapter)
            
            categorySpinner.setText(item.category.displayName, false)
            
            categorySpinner.setOnItemClickListener { _, _, position, _ ->
                val selectedCategoryName = adapter.getItem(position)
                val selectedCategory = AppCategory.values().find { it.displayName == selectedCategoryName }
                selectedCategory?.let {
                    onCategoryChanged(item.packageName, it)
                }
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<AppCategoryItem>() {
        override fun areItemsTheSame(oldItem: AppCategoryItem, newItem: AppCategoryItem): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        
        override fun areContentsTheSame(oldItem: AppCategoryItem, newItem: AppCategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
