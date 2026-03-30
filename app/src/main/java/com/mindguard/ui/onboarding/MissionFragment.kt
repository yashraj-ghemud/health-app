package com.mindguard.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mindguard.databinding.FragmentOnboardingMissionBinding

class MissionFragment : Fragment() {
    
    private var _binding: FragmentOnboardingMissionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var missionAdapter: MissionPagerAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingMissionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupIndicators()
    }
    
    private fun setupViewPager() {
        missionAdapter = MissionPagerAdapter(requireContext())
        binding.viewPagerMission.adapter = missionAdapter
        
        binding.viewPagerMission.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
    }
    
    private fun setupIndicators() {
        updateIndicators(0)
        
        binding.indicator1.setOnClickListener {
            binding.viewPagerMission.currentItem = 0
        }
        
        binding.indicator2.setOnClickListener {
            binding.viewPagerMission.currentItem = 1
        }
        
        binding.indicator3.setOnClickListener {
            binding.viewPagerMission.currentItem = 2
        }
    }
    
    private fun updateIndicators(position: Int) {
        val indicators = listOf(binding.indicator1, binding.indicator2, binding.indicator3)
        indicators.forEachIndexed { index, indicator ->
            indicator.isSelected = index == position
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
