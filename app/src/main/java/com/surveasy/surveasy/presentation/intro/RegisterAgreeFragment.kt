package com.surveasy.surveasy.presentation.intro

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.surveasy.surveasy.R
import com.surveasy.surveasy.databinding.FragmentRegisterAgreeBinding
import com.surveasy.surveasy.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterAgreeFragment :
    BaseFragment<FragmentRegisterAgreeBinding>(R.layout.fragment_register_agree) {
    private val viewModel: RegisterViewModel by viewModels()


    override fun initView() = with(binding) {
        vm = viewModel
    }

    override fun initEventObserver() {
        repeatOnStarted {
            viewModel.events.collect { event ->
                when (event) {
                    is RegisterEvents.NavigateToRegisterWarn ->
                        findNavController().navigate(RegisterAgreeFragmentDirections.actionRegisterAgreeFragmentToRegisterWarnFragment())

                    else -> Unit
                }
            }
        }
    }

    override fun initData() {

    }

}