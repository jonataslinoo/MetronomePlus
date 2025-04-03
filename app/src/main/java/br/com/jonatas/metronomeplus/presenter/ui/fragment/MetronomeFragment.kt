package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.jonatas.metronomeplus.R
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
import br.com.jonatas.metronomeplus.domain.usecase.SetBpmUseCaseImpl
import br.com.jonatas.metronomeplus.domain.usecase.TogglePlayPauseUseCaseImpl
import br.com.jonatas.metronomeplus.presenter.util.setHighlightDrawableOnTouchListener
import br.com.jonatas.metronomeplus.presenter.ui.custom.OnBeatClickListener
import br.com.jonatas.metronomeplus.presenter.ui.custom.OnCircularSeekBarChangeListener
import br.com.jonatas.metronomeplus.presenter.util.AngularVelocityTrackerImpl
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
        setupInitializationAndListeners()
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
                            setUiStateReady(uiState)
                        }

                        is MetronomeViewModel.MetronomeState.Error -> {
                            setUiStateError(uiState)
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
        val setBpmUseCase = SetBpmUseCaseImpl()
        val addBeatUseCase = AddBeatUseCaseImpl()
        val removeBeatUseCase = RemoveBeatUseCaseImpl()
        val togglePlayPauseUseCase = TogglePlayPauseUseCaseImpl()
        val increaseMeasureCounter = IncreaseMeasureCounterImpl()
        val nextBeatStateUseCase = NextBeatStateUseCaseImpl()

        val viewModelFactory = MetronomeViewModelFactory(
            metronomeEngine = metronomeEngine,
            getMeasureUseCase = getMeasureUseCase,
            increaseBpmUseCase = increaseBpmUseCase,
            decreaseBpmUseCase = decreaseBpmUseCase,
            setBpmUseCase = setBpmUseCase,
            addBeatUseCase = addBeatUseCase,
            removeBeatUseCase = removeBeatUseCase,
            togglePlayPauseUseCase = togglePlayPauseUseCase,
            nextBeatStateUseCase = nextBeatStateUseCase,
            increaseMeasureCounter = increaseMeasureCounter
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[MetronomeViewModel::class.java]
    }

    private fun setupInitializationAndListeners() {
        with(binding) {
            btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }

            beatListView.setOnBeatClickListener(object : OnBeatClickListener {
                override fun onBeatClick(index: Int) {
                    viewModel.changeBeatState(index)
                }
            })

            circularSeekBar.apply {
                createAngularVelocityTracker(AngularVelocityTrackerImpl())

                setOnCircularSeekBarChangeListener(object : OnCircularSeekBarChangeListener {
                    override fun onProgressChanged(value: Float) {
                        viewModel.setBpm(value.toInt())
                    }
                })
            }

            viewTimeSignatureButtonItem.run {
                btnLessTimeSig.apply {
                    setHighlightDrawableOnTouchListener(
                        newDrawable = getDrawable(requireContext(), R.drawable.ic_remove_highlight),
                        oldDrawable = getDrawable(requireContext(), R.drawable.ic_remove_white)
                    ) { viewModel.removeBeat() }
                }
                btnMoreTimeSig.apply {
                    setHighlightDrawableOnTouchListener(
                        newDrawable = getDrawable(requireContext(), R.drawable.ic_add_highlight),
                        oldDrawable = getDrawable(requireContext(), R.drawable.ic_add_white)
                    ) { viewModel.addBeat() }
                }
            }
        }
    }

    private fun setUiStateReady(uiState: MetronomeViewModel.MetronomeState.Ready) {
        val measureUi = uiState.measure

        binding.apply {
            btnPlayPause.apply {
                val playDrawable = getDrawable(requireContext(), R.drawable.ic_play_white)
                val pauseDrawable = getDrawable(requireContext(), R.drawable.ic_pause_highlight)

                if (measureUi.isPlaying) {
                    background = getDrawable(requireContext(), R.drawable.metronome_button_pressed)
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        pauseDrawable, null, null, null
                    )
                } else {
                    background = getDrawable(requireContext(), R.drawable.metronome_button)
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        playDrawable, null, null, null
                    )
                }
            }

            measureUi.bpm.toString().run {
                viewBpmItem.apply {
                    numberOne.text = getOrNull(0)?.toString() ?: ""
                    numberTwo.text = getOrNull(1)?.toString() ?: ""
                    numberThree.text = getOrNull(2)?.toString() ?: ""
                }
            }

            viewTimeSignatureItem.apply {
                measureUi.beats.size.toString().run {

                    if (getOrNull(1) == null) {
                        numeratorOne.text = ""
                        numeratorTwo.text = get(0).toString()
                    } else {
                        numeratorOne.text = get(0).toString()
                        numeratorTwo.text = get(1).toString()
                    }
                    denominator.text = "4"
                }
            }

            beatListView.updateBpm(measureUi.bpm)
            beatListView.updateBeats(measureUi.beats)
        }
    }

    private fun setUiStateError(uiState: MetronomeViewModel.MetronomeState.Error) {
        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}