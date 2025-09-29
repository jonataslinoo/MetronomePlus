package br.com.jonatas.metronomeplus.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.FragmentFolderFormBinding
import br.com.jonatas.metronomeplus.presenter.extension.setAlphaForState
import br.com.jonatas.metronomeplus.presenter.extension.showMessage
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import br.com.jonatas.metronomeplus.presenter.ui.adapter.FolderFormSongsAdapter
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperManager
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import br.com.jonatas.metronomeplus.presenter.viewmodel.FolderFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FolderFormFragment : Fragment() {

    private var _binding: FragmentFolderFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FolderFormViewModel by viewModels()

    @Inject
    lateinit var songsAdapter: FolderFormSongsAdapter

    @Inject
    lateinit var itemTouchHelperManager: ItemTouchHelperManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFolderFormBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenuActionBar()
        setupBackButton()
        setupRecyclerViewAndSearchView()
        setupListeners()

        setupObserverUiState()
    }

    private fun setupObserverUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is UiState.Loading -> {
                            /*Nothing*/
                        }

                        is UiState.Ready -> {
                            setUiStateReady(uiState.result)
                        }

                        is UiState.Error -> {

                        }
                    }
                }
            }
        }
    }

    private fun setUiStateReady(formUiState: FolderFormUiState) {
        binding.folderName.setText(formUiState.folderUi.name)
        binding.toolbar.title = when (formUiState.barTitle) {
            is FolderFormTitleMode.NewFolder -> getString(R.string.new_folder)
            is FolderFormTitleMode.ViewFolder -> getString(R.string.view_folder)
            is FolderFormTitleMode.EditFolder -> getString(R.string.edit_folder)
        }

        songsAdapter.submitList(formUiState.songsUi)

        setEnabledFields(formUiState.editableState)
        requireActivity().invalidateMenu()
    }

    private fun setupRecyclerViewAndSearchView() {
        binding.folderFormSongsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            adapter = songsAdapter

            itemTouchHelperManager.itemTouchHelper.attachToRecyclerView(this)
        }

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchSongInfo(newText.orEmpty())
                return false
            }
        })
    }

    private fun setEnabledFields(editableState: EditableState) {
        binding.folderName.isEnabled = editableState.isEditMode
        binding.folderName.setAlphaForState(editableState.isEditMode)
        binding.textInputLayoutName.setAlphaForState(editableState.isEditMode)

        songsAdapter.editableState = editableState
    }

    private fun setupListeners() {
        binding.apply {
            songsAdapter.setCallbacks(
                callbacks = SongCallbacks(
                    onItemClicked = { songId -> showMessage(root, "onClick $songId") },
                    onItemMenuClicked = { songId, view ->
                        showMessage(root, "onClick menu $songId - $view")
                    },
                    onItemMove = { fromPosition, toPosition -> },
                    onItemSelectionToggle = { songId -> },
                    onListEditMode = { songId, enable ->
                        showMessage(binding.root, "$songId")
                        viewModel.enableListEditMode(enable) },
                )
            )
        }
    }

    private fun setupMenuActionBar() {
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.form_folder_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                val currentState = viewModel.uiState.value
                if (currentState is UiState.Ready) {
                    val isEditMode = currentState.result.editableState.isEditMode
                    setVisibilityMenu(
                        menu = menu,
                        showSaveMenu = isEditMode,
                        showEditMenu = !isEditMode
                    )
                } else {
                    setVisibilityMenu(menu = menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.saveFolder -> {
                        showMessage(binding.root, "Testando")
                        true
                    }

                    R.id.editFolder -> {
                        viewModel.enableEditMode()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setVisibilityMenu(
        menu: Menu,
        showSaveMenu: Boolean = false,
        showEditMenu: Boolean = false,
    ) {
        menu.findItem(R.id.saveFolder).isVisible = showSaveMenu
        menu.findItem(R.id.editFolder).isVisible = showEditMenu
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}