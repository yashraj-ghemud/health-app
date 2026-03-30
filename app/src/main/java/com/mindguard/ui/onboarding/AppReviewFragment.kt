package com.mindguard.ui.onboarding

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindguard.data.model.AppCategory
import kotlinx.coroutines.launch
import com.mindguard.databinding.FragmentAppReviewBinding
import com.mindguard.ui.onboarding.adapters.AppCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppReviewFragment : Fragment() {
    
    private var _binding: FragmentAppReviewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: OnboardingViewModel by activityViewModels()
    
    @Inject
    lateinit var appCategoryRepository: com.mindguard.data.repository.AppCategoryRepository
    
    private lateinit var appCategoryAdapter: AppCategoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppReviewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadInstalledApps()
    }
    
    private fun setupRecyclerView() {
        appCategoryAdapter = AppCategoryAdapter { packageName, category ->
            // Update app category
            updateAppCategory(packageName, category)
        }
        
        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appCategoryAdapter
        }
    }
    
    private fun loadInstalledApps() {
        val packageManager = requireContext().packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        lifecycleScope.launch {
            val userApps = installedApps.filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }.map { app ->
                val packageName = app.packageName
                val appLabel = packageManager.getApplicationLabel(app).toString()
                val category = appCategoryRepository.getCategoryForPackage(packageName) ?: AppCategory.NEUTRAL
                
                AppCategoryItem(
                    packageName = packageName,
                    appLabel = appLabel,
                    category = category,
                    icon = packageManager.getApplicationIcon(app)
                )
            }.sortedBy { it.appLabel }
            
            appCategoryAdapter.submitList(userApps)
        }
    }
    
    private fun updateAppCategory(packageName: String, category: AppCategory) {
        // Update the category in the repository
        // This would be implemented to save the user's preference
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    data class AppCategoryItem(
        val packageName: String,
        val appLabel: String,
        val category: AppCategory,
        val icon: android.graphics.drawable.Drawable
    )
}
