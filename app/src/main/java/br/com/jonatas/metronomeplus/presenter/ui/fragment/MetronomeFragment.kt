package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.com.jonatas.metronomeplus.data.engine.MetronomeEngineImpl
import br.com.jonatas.metronomeplus.data.provider.AssetProviderImpl
import br.com.jonatas.metronomeplus.data.provider.AudioSettingProviderImpl
import br.com.jonatas.metronomeplus.databinding.FragmentMetronomeBinding
import br.com.jonatas.metronomeplus.presenter.ui.viewmodel.MetronomeViewModel
import br.com.jonatas.metronomeplus.presenter.ui.viewmodel.MetronomeViewModelFactory

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

        val viewModelFactory = MetronomeViewModelFactory(
            MetronomeEngineImpl(),
            AssetProviderImpl(requireContext().applicationContext),
            AudioSettingProviderImpl(requireContext().applicationContext)
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[MetronomeViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}