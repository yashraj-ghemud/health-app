package com.mindguard.ui.settings.rules

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindguard.R
import com.mindguard.databinding.ActivityRulesBinding
import com.mindguard.ui.settings.rules.adapters.RulesAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RulesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRulesBinding
    private val viewModel: RulesViewModel by viewModels()
    private lateinit var rulesAdapter: RulesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadRules()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Usage Rules"
    }
    
    private fun setupRecyclerView() {
        rulesAdapter = RulesAdapter(
            onRuleToggled = { rule, enabled ->
                viewModel.toggleRule(rule.id, enabled)
            },
            onRuleClicked = { rule ->
                editRule(rule)
            }
        )
        
        binding.recyclerViewRules.apply {
            layoutManager = LinearLayoutManager(this@RulesActivity)
            adapter = rulesAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.rules.observe(this) { rules ->
            rulesAdapter.submitList(rules)
            binding.emptyState.isVisible = rules.isEmpty()
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.recyclerViewRules.isVisible = !isLoading
        }
    }
    
    private fun loadRules() {
        viewModel.loadRules()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_rules, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_add_rule -> {
                createNewRule()
                true
            }
            R.id.action_reset_defaults -> {
                resetToDefaults()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun createNewRule() {
        val intent = Intent(this, CreateRuleActivity::class.java)
        startActivity(intent)
    }

    private fun editRule(rule: com.mindguard.data.model.UsageRule) {
        val intent = Intent(this, CreateRuleActivity::class.java).apply {
            putExtra("rule_id", rule.id)
        }
        startActivity(intent)
    }
    
    private fun deleteRule(rule: com.mindguard.data.model.UsageRule) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Rule")
            .setMessage("Are you sure you want to delete this rule?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRule(rule.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetToDefaults() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Rules")
            .setMessage("This will reset all rules to their default values. Your custom rules will be lost.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetToDefaults()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
