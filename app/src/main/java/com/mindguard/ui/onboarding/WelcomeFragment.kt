package com.mindguard.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mindguard.databinding.FragmentOnboardingWelcomeBinding

class WelcomeFragment : Fragment() {
    
    private var _binding: FragmentOnboardingWelcomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.tvTitle.text = "Welcome to MindGuard"
        binding.tvSubtitle.text = "Your phone. Your rules."
        binding.tvDescription.text = "Take control of your digital life with intelligent monitoring and gentle interventions that help you build healthier habits."
        
        // Animate elements
        binding.ivLogo.alpha = 0f
        binding.tvTitle.alpha = 0f
        binding.tvSubtitle.alpha = 0f
        binding.tvDescription.alpha = 0f
        
        binding.ivLogo.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
        
        binding.tvTitle.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start()
        
        binding.tvSubtitle.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .start()
        
        binding.tvDescription.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(600)
            .start()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
