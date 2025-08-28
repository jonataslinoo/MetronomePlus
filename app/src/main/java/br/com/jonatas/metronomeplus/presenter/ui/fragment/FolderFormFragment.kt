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
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.FragmentFolderFormBinding
import br.com.jonatas.metronomeplus.presenter.extension.enabledAllChildren
import br.com.jonatas.metronomeplus.presenter.extension.showMessage
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormTitleMode
import br.com.jonatas.metronomeplus.presenter.model.folder.FolderFormUiState
import br.com.jonatas.metronomeplus.presenter.model.states.UiState
import br.com.jonatas.metronomeplus.presenter.viewmodel.FolderFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderFormFragment : Fragment() {

    private var _binding: FragmentFolderFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FolderFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderFormBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        setupObserverUiState()
        setupMenuActionBar()
        setupBackButton()
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

        setEnabledFields(formUiState.isEditMode)

        requireActivity().invalidateMenu()
    }

    private fun setEnabledFields(enable: Boolean = false) {
        binding.folderName.isEnabled = enable
        binding.searchView.enabledAllChildren(enable)
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
                    val isEditMode = currentState.result.isEditMode
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
                        viewModel.onEditClicked()
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
        showEditMenu: Boolean = false
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