package com.mindguard.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.mindguard.data.model.AppCategory
import com.mindguard.databinding.FragmentDashboardBinding
import com.mindguard.ui.dashboard.adapters.DashboardAdapter
import com.mindguard.ui.dashboard.viewholders.DashboardItemType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    
    @Inject
    lateinit var wellnessScoreCalculator: com.mindguard.domain.engine.WellnessScoreCalculator
    
    private lateinit var dashboardAdapter: DashboardAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
        refreshData()
    }
    
    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter()
        binding.recyclerViewDashboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dashboardAdapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardItems.collect { items ->
                dashboardAdapter.submitList(items)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.recyclerViewDashboard.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }
    
    private fun refreshData() {
        viewModel.refreshDashboardData()
    }
    
    override fun onResume() {
        super.onResume()
        refreshData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
