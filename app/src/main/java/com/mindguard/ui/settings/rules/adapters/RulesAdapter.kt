package com.mindguard.ui.settings.rules.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mindguard.R
import com.mindguard.data.model.UsageRule

class RulesAdapter(
    private val onRuleToggled: (UsageRule, Boolean) -> Unit,
    private val onRuleClicked: (UsageRule) -> Unit
) : ListAdapter<UsageRule, RulesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<UsageRule>() {
        override fun areItemsTheSame(oldItem: UsageRule, newItem: UsageRule): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UsageRule, newItem: UsageRule): Boolean = oldItem == newItem
    }
}
