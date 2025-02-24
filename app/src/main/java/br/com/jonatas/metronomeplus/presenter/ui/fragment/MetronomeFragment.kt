package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.jonatas.metronomeplus.data.engine.MetronomeEngineImpl
import br.com.jonatas.metronomeplus.data.provider.AssetProviderImpl
import br.com.jonatas.metronomeplus.data.provider.AudioSettingProviderImpl
import br.com.jonatas.metronomeplus.data.repository.MeasureRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.MeasureDataSourceImpl
import br.com.jonatas.metronomeplus.databinding.FragmentMetronomeBinding
import br.com.jonatas.metronomeplus.presenter.viewmodel.MetronomeViewModel
import br.com.jonatas.metronomeplus.presenter.viewmodel.MetronomeViewModelFactory
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
                        is MetronomeViewModel.MetronomeState.Loading -> {
                            // Initialize state flow
                        }

                        is MetronomeViewModel.MetronomeState.Ready -> {
                            binding.apply {
                                btnPlayPause.text =
                                    if (uiState.measure.isPlaying) "Pause" else "Play"

                                tvBpm.text = uiState.measure.bpm.toString()
                            }
                        }

                        is MetronomeViewModel.MetronomeState.Error -> {
                            Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT)
                                .show()
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
            MeasureRepositoryImpl(MeasureDataSourceImpl())
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[MetronomeViewModel::class.java]
    }

    private fun setupClickListener() {
        binding.btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.btnMoreOne.setOnClickListener { viewModel.increaseBpm(1) }
        binding.btnMoreThen.setOnClickListener { viewModel.increaseBpm(10) }
        binding.btnMinusOne.setOnClickListener { viewModel.decreaseBpm(-1) }
        binding.btnMinusThen.setOnClickListener { viewModel.decreaseBpm(-10) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}