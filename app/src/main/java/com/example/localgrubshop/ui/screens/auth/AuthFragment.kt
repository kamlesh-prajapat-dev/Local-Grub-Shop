package com.example.localgrubshop.ui.screens.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.databinding.FragmentAuthBinding
import com.example.localgrubshop.domain.models.failure.GetReqDomainFailure
import com.example.localgrubshop.domain.models.failure.WriteReqDomainFailure
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListener()
        observeViewModel()
    }

    private fun setupListener() {
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }

        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(username, password)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when(it) {
                        is AuthUIState.Failure -> {
                            when(val failure = it.failure) {
                                GetReqDomainFailure.Cancelled -> Unit
                                is GetReqDomainFailure.DataNotFound -> {
                                    binding.etPassword.error = failure.message
                                }
                                GetReqDomainFailure.InvalidRequest -> {
                                    Toast.makeText(requireContext(), "Invalid Request", Toast.LENGTH_LONG).show()
                                }
                                GetReqDomainFailure.NoInternet -> {
                                    Toast.makeText(requireContext(), "No Internet", Toast.LENGTH_LONG).show()
                                }
                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }
                        AuthUIState.Idle -> {
                            onSetLoading(false)
                        }
                        AuthUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is AuthUIState.Success -> {
                            viewModel.saveToken(it.adminUser)
                            onSetLoading(false)
                        }
                        is AuthUIState.ValidationError -> {
                            val validateMsgForUsername = it.validateMsgForUsername
                            val validateMsgForPassword = it.validateMsgForPassword
                            if (validateMsgForUsername.isNotBlank()) {
                                binding.tilEmail.error = it.validateMsgForUsername
                            }
                            if (validateMsgForPassword.isNotBlank()) {
                                binding.tilPassword.error = it.validateMsgForPassword
                            }

                            onSetLoading(false)
                        }

                        is AuthUIState.UpdateTokenFailure -> {
                            when(val failure = it.failure) {
                                WriteReqDomainFailure.Aborted -> {
                                    Toast.makeText(requireContext(), "Aborted", Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.AlreadyExists -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.Cancelled -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.DataLoss -> {
                                    Toast.makeText(requireContext(), "Data Loss", Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.DeadlineExceeded -> {
                                    Toast.makeText(requireContext(), "Deadline Exceed", Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.FailedPrecondition -> {
                                    Toast.makeText(requireContext(), "Failed Precondition", Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.Internal -> {
                                    Toast.makeText(requireContext(), "Internal", Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.InvalidArgument -> {
                                    Toast.makeText(requireContext(), "Invalid Argument", Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.NetworkUnavailable -> {
                                    Toast.makeText(requireContext(), "Network Unavailable", Toast.LENGTH_LONG).show()

                                }
                                is WriteReqDomainFailure.NotFound -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.OutOfRange -> {}
                                is WriteReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.ResourceExhausted -> {
                                    Toast.makeText(requireContext(), "Resource Exhausted", Toast.LENGTH_LONG).show()

                                }
                                is WriteReqDomainFailure.Unauthenticated -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.Unimplemented -> {
                                    Toast.makeText(requireContext(), "Unimplemented", Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        is AuthUIState.UpdateTokenSuccess -> {
                            val action = AuthFragmentDirections.actionAuthFragmentToHomeFragment()
                            findNavController().navigate(action)
                            onSetLoading(false)
                        }

                        is AuthUIState.DataLoadFailure -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}