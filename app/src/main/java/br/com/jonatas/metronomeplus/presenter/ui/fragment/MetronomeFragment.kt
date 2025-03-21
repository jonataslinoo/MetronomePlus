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
import br.com.jonatas.metronomeplus.domain.usecase.AddBeatUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.DecreaseBpmUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.GetMeasureUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseBpmUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.IncreaseMeasureCounterImpl
import br.com.jonatas.metronomeplus.domain.usecase.NextBeatStateUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.RemoveBeatUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.TogglePlayPauseUseCaseImpl
import br.com.jonatas.metronomeplus.presenter.ui.custom.OnBeatClickListener
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
        setupObserverUiState()
        setupObserverMeasureProgressUiState()
        setupClickListener()
    }

    private fun setupObserverUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MetronomeViewModel.MetronomeState.Loading -> {
                            // Initialize state flow
                        }

                        is MetronomeViewModel.MetronomeState.Ready -> {
                            binding.apply {
                                val measureUi = uiState.measure
                                btnPlayPause.text = if (measureUi.isPlaying) "Pause" else "Play"
                                tvBpm.text = measureUi.bpm.toString()
                                beatCounter.text = measureUi.beats.size.toString()

                                beatListView.updateBeats(measureUi.beats)
                                beatListView.updateBpm(measureUi.bpm)
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

    private fun setupObserverMeasureProgressUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measureProgressUiState.collect { measureProgressUiState ->
                    binding.apply {
                        beatListView.nextBeat(measureProgressUiState.currentBeat)
                    }
                }
            }
        }
    }

    private fun setupViewModel() {
        val assetProvider = AssetProviderImpl(requireContext().applicationContext)
        val audioSettingsProvider = AudioSettingProviderImpl(requireContext().applicationContext)
        val metronomeEngine = MetronomeEngineImpl(assetProvider, audioSettingsProvider)
        val measureRepositoryImpl = MeasureRepositoryImpl(MeasureDataSourceImpl())
        val getMeasureUseCase = GetMeasureUseCaseImpl(measureRepositoryImpl)
        val increaseBpmUseCase = IncreaseBpmUseCaseImpl()
        val decreaseBpmUseCase = DecreaseBpmUseCaseImpl()
        val addBeatUseCase = AddBeatUseCaseImpl()
        val removeBeatUseCase = RemoveBeatUseCaseImpl()
        val togglePlayPauseUseCase = TogglePlayPauseUseCaseImpl()
        val increaseMeasureCounter = IncreaseMeasureCounterImpl()
        val nextBeatStateUseCase = NextBeatStateUseCaseImpl()

        val viewModelFactory = MetronomeViewModelFactory(
            metronomeEngine = metronomeEngine,
            getMeasureUseCase = getMeasureUseCase,
            decreaseBpmUseCase = decreaseBpmUseCase,
            increaseBpmUseCase = increaseBpmUseCase,
            addBeatUseCase = addBeatUseCase,
            removeBeatUseCase = removeBeatUseCase,
            togglePlayPauseUseCase = togglePlayPauseUseCase,
            nextBeatStateUseCase = nextBeatStateUseCase,
            increaseMeasureCounter = increaseMeasureCounter
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[MetronomeViewModel::class.java]
    }

    private fun setupClickListener() {
        binding.btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.btnMoreOne.setOnClickListener { viewModel.increaseBpm(1) }
        binding.btnMoreThen.setOnClickListener { viewModel.increaseBpm(10) }
        binding.btnMinusOne.setOnClickListener { viewModel.decreaseBpm(1) }
        binding.btnMinusThen.setOnClickListener { viewModel.decreaseBpm(10) }
        binding.btnMoreOneBeat.setOnClickListener { viewModel.addBeat() }
        binding.btnMinusOneBeat.setOnClickListener { viewModel.removeBeat() }
        binding.beatListView.setOnBeatClickListener(object : OnBeatClickListener {
            override fun onBeatClick(index: Int) {
                viewModel.changeBeatState(index)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}