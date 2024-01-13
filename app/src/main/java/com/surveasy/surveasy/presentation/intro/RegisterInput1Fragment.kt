package com.surveasy.surveasy.presentation.intro

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.surveasy.surveasy.R
import com.surveasy.surveasy.databinding.FragmentRegisterInput1Binding
import com.surveasy.surveasy.presentation.base.BaseFragment
import com.surveasy.surveasy.presentation.util.showCalendarDatePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterInput1Fragment :
    BaseFragment<FragmentRegisterInput1Binding>(R.layout.fragment_register_input1) {
    private val viewModel: RegisterViewModel by viewModels()

    override fun initData() = Unit

    override fun initEventObserver() {
        repeatOnStarted {
            viewModel.events.collect { event ->
                when (event) {
                    is RegisterEvents.NavigateToRegisterInput2 -> findNavController().navigate(
                        RegisterInput1FragmentDirections.actionRegisterInput1FragmentToRegisterInput2Fragment()
                    )

                    is RegisterEvents.NavigateToBack -> findNavController().navigateUp()
                    else -> Unit
                }
            }
        }
    }

    override fun initView() = with(binding) {
        initDatePicker()
        initInflowPathSpinner()
        vm = viewModel
        lifecycleOwner = viewLifecycleOwner

    }

    private fun initDatePicker() {
        binding.ivCalendar.setOnClickListener {
            showCalendarDatePicker(parentFragmentManager) {
                viewModel.setBirth(it)
            }
        }
    }

    private fun initInflowPathSpinner() = with(binding) {
        val inflowPathList = resources.getStringArray(R.array.inflowPath)
        val inflowPathAdapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            inflowPathList
        )

        sInflow.apply {
            adapter = inflowPathAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.setInflow(inflowPathList[position])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
        }
    }
}
