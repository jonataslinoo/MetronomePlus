package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.FragmentLibraryBinding
import br.com.jonatas.metronomeplus.presenter.ui.adapter.LibraryFoldersAdapter
import br.com.jonatas.metronomeplus.presenter.viewmodel.LibraryVieModelFactory
import br.com.jonatas.metronomeplus.presenter.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupViewModel()
        setupObserverUiState()
        setupInitializationAndListeners()
    }

    private fun setupMenu() {
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.library_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.newFolder -> {
                        //Open the form to create a new folder
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupViewModel() {
        val viewModelFactory = LibraryVieModelFactory()

        viewModel = ViewModelProvider(this, viewModelFactory)[LibraryViewModel::class]
    }

    private fun setupObserverUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is LibraryViewModel.LibraryState.Loading -> {
                            /*Nothing*/
                        }

                        is LibraryViewModel.LibraryState.Ready -> {
                            val libraryAdapter =
                                LibraryFoldersAdapter(requireContext(), uiState.foldersUi)
                            val linearLayoutManager =
                                LinearLayoutManager(requireContext(), VERTICAL, false)
                            binding.recyclerView.apply {
                                adapter = libraryAdapter
                                layoutManager = linearLayoutManager
                            }
                        }

                        is LibraryViewModel.LibraryState.Error -> {
                            setUiStateError(uiState)
                        }
                    }
                }
            }
        }
    }

    private fun setupInitializationAndListeners() {
    }

    private fun setUiStateError(uiState: LibraryViewModel.LibraryState.Error) {
        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }
}