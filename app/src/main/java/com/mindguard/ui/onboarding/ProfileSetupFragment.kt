package com.mindguard.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mindguard.R
import com.mindguard.data.model.StrictLevel
import com.mindguard.databinding.FragmentProfileSetupBinding

class ProfileSetupFragment : Fragment() {
    
    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: OnboardingViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // Setup goal spinner
        val goals = arrayOf(
            "Career Focus",
            "Study",
            "Mental Health",
            "General Wellness",
            "Custom"
        )
        val goalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, goals)
        binding.spinnerGoal.setAdapter(goalAdapter)
        
        // Setup strict level spinner
        val strictLevels = StrictLevel.values().map { it.name.replace("_", " ") }
        val strictAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, strictLevels)
        binding.spinnerStrictLevel.setAdapter(strictAdapter)
        
        // Set default values
        binding.spinnerGoal.setText("General Wellness", false)
        binding.spinnerStrictLevel.setText("BALANCED", false)
        binding.etWorkHoursStart.setText("09:00")
        binding.etWorkHoursEnd.setText("18:00")
    }
    
    private fun setupListeners() {
        binding.spinnerGoal.setOnItemClickListener { _, _, position, _ ->
            val selectedGoal = binding.spinnerGoal.text.toString()
            viewModel.setUserGoal(selectedGoal)
        }
        
        binding.spinnerStrictLevel.setOnItemClickListener { _, _, position, _ ->
            val selectedLevel = binding.spinnerStrictLevel.text.toString().replace(" ", "_")
            val strictLevel = StrictLevel.valueOf(selectedLevel)
            viewModel.setStrictLevel(strictLevel)
        }
        
        binding.etWorkHoursStart.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val startTime = binding.etWorkHoursStart.text.toString()
                val endTime = binding.etWorkHoursEnd.text.toString()
                viewModel.setWorkHours(startTime, endTime)
            }
        }
        
        binding.etWorkHoursEnd.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val startTime = binding.etWorkHoursStart.text.toString()
                val endTime = binding.etWorkHoursEnd.text.toString()
                viewModel.setWorkHours(startTime, endTime)
            }
        }
        
        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = binding.etName.text.toString()
                viewModel.setUserName(name)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
