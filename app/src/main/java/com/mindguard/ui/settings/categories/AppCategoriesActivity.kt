package com.mindguard.ui.settings.categories

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.mindguard.R
import com.mindguard.databinding.ActivityAppCategoriesBinding
import com.mindguard.ui.settings.categories.adapters.AppCategoriesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AppCategoriesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAppCategoriesBinding
    private val viewModel: AppCategoriesViewModel by viewModels()
    private lateinit var appCategoriesAdapter: AppCategoriesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        loadAppCategories()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "App Categories"
    }
    
    private fun setupRecyclerView() {
        appCategoriesAdapter = AppCategoriesAdapter { app, newCategory ->
            viewModel.updateAppCategory(app.packageName, newCategory)
        }
        
        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(this@AppCategoriesActivity)
            adapter = appCategoriesAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.appCategories.observe(this) { categories ->
            appCategoriesAdapter.submitList(categories)
            binding.emptyState.isVisible = categories.isEmpty()
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.recyclerViewCategories.isVisible = !isLoading
        }
    }
    
    private fun loadAppCategories() {
        viewModel.loadAppCategories()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app_categories, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterCategories(query ?: "")
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterCategories(newText ?: "")
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_reset -> {
                showResetDialog()
                true
            }
            R.id.action_export -> {
                exportCategories()
                true
            }
            R.id.action_import -> {
                importCategories()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showResetDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Categories")
            .setMessage("This will reset all app categories to their default values. Your custom categories will be lost.")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetToDefaults()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportCategories() {
        lifecycleScope.launch {
            try {
                viewModel.exportCategories()
                showExportSuccessDialog("Downloads/categories.csv")
            } catch (e: Exception) {
                Timber.e(e, "Error exporting categories")
                showExportErrorDialog()
            }
        }
    }
    
    private fun importCategories() {
        // This would open a file picker to import categories
        // For now, show a placeholder dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Categories")
            .setMessage("Import functionality will be available in the next update.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showExportSuccessDialog(exportPath: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Export Successful")
            .setMessage("App categories exported to:\n$exportPath")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showExportErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Export Failed")
            .setMessage("Failed to export app categories. Please try again.")
            .setPositiveButton("OK", null)
            .show()
    }
}
