package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.jonatas.metronomeplus.data.engine.MetronomeEngineImpl
import br.com.jonatas.metronomeplus.data.provider.AssetProviderImpl
import br.com.jonatas.metronomeplus.data.provider.AudioSettingProviderImpl
import br.com.jonatas.metronomeplus.data.repository.MeasureRepositoryImpl
import br.com.jonatas.metronomeplus.databinding.FragmentMetronomeBinding
import br.com.jonatas.metronomeplus.presenter.ui.viewmodel.MetronomeViewModel
import br.com.jonatas.metronomeplus.presenter.ui.viewmodel.MetronomeViewModelFactory
import kotlinx.coroutines.launch

class MetronomeFragment : Fragment() {

    private var _binding: FragmentMetronomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MetronomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMetronomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupObservers()
        setupClickListener()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MetronomeViewModel.MetronomeState.Initial -> {
                            // Initialize state flow
                        }

                        is MetronomeViewModel.MetronomeState.Ready -> {
                            binding.btnPlayPause.text =
                                if (uiState.measure.isPlaying) "Pause" else "Play"
                        }

                        is MetronomeViewModel.MetronomeState.Error -> {

                        }
                    }
                }
            }
        }
    }

    private fun setupViewModel() {
        val viewModelFactory = MetronomeViewModelFactory(
            MetronomeEngineImpl(),
            AssetProviderImpl(requireContext().applicationContext),
            AudioSettingProviderImpl(requireContext().applicationContext),
            MeasureRepositoryImpl()
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[MetronomeViewModel::class.java]
    }

    private fun setupClickListener() {
        binding.btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}