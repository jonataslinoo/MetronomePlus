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
import br.com.jonatas.metronomeplus.data.repository.FolderRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.FolderDataSourceImpl
import br.com.jonatas.metronomeplus.databinding.FragmentLibraryBinding
import br.com.jonatas.metronomeplus.domain.usecase.library.GetFoldersUseCaseImpl
import br.com.jonatas.metronomeplus.presenter.interfaces.OnFolderClickListener
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.LibraryFoldersAdapter
import br.com.jonatas.metronomeplus.presenter.viewmodel.LibraryVieModelFactory
import br.com.jonatas.metronomeplus.presenter.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var foldersAdapter: LibraryFoldersAdapter

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

        setupViewModel()
        setupToolbarMenu()
        setupRecyclerViewLibraryFolders()
        setupObserverUiState()
        setupInitializationAndListeners()
    }

    private fun setupToolbarMenu() {
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.library_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.newFolder -> {
                        // Open the form to create a new folder
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupViewModel() {
        val dataSource = FolderDataSourceImpl()
        val repository = FolderRepositoryImpl(dataSource)
        val getFoldersUseCase = GetFoldersUseCaseImpl(repository)
        val viewModelFactory = LibraryVieModelFactory(getFoldersUseCase = getFoldersUseCase)

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
                            setUiStateReady(uiState)
                        }

                        is LibraryViewModel.LibraryState.Error -> {
                            setUiStateError(uiState)
                        }
                    }
                }
            }
        }
    }

    private fun setUiStateReady(uiState: LibraryViewModel.LibraryState.Ready) {
        foldersAdapter.submitList(uiState.foldersUi)
    }

    private fun setupRecyclerViewLibraryFolders() {
        foldersAdapter = LibraryFoldersAdapter(object : OnFolderClickListener {
            override fun onFolderClicked(folderUiModel: FolderUiModel) {
                // TODO("Not yet implemented")
            }

            override fun onFolderOptionsClicked(folderUiModel: FolderUiModel, anchorView: View) {
                // TODO("Not yet implemented")
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
            adapter = foldersAdapter
        }
    }


    private fun setupInitializationAndListeners() {
    }

    private fun setUiStateError(uiState: LibraryViewModel.LibraryState.Error) {
        Toast.makeText(
            requireContext(),
            getString(R.string.message_error, uiState.message), Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }
}