package com.mindguard.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mindguard.databinding.FragmentAchievementsBinding
import com.mindguard.ui.achievements.adapters.AchievementsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AchievementsFragment : Fragment() {
    
    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AchievementsViewModel by viewModels()
    
    private lateinit var achievementsAdapter: AchievementsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
        refreshAchievements()
    }
    
    private fun setupRecyclerView() {
        achievementsAdapter = AchievementsAdapter()
        binding.recyclerViewAchievements.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = achievementsAdapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.achievements.collect { achievements ->
                achievementsAdapter.submitList(achievements)
                updateProgressUI()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.achievementProgress.collect { progress ->
                progress?.let {
                    binding.progressBar.progress = it.percentage
                    binding.tvProgress.text = "${it.unlocked}/${it.total}"
                    binding.tvPercentage.text = "${it.percentage}%"
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.recyclerViewAchievements.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }
    
    private fun updateProgressUI() {
        val progress = viewModel.achievementProgress.value
        progress?.let {
            binding.progressBar.progress = it.percentage
            binding.tvProgress.text = "${it.unlocked}/${it.total}"
            binding.tvPercentage.text = "${it.percentage}%"
        }
    }
    
    private fun refreshAchievements() {
        viewModel.refreshAchievements()
    }
    
    override fun onResume() {
        super.onResume()
        refreshAchievements()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
