package art.intel.soft.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import art.intel.soft.R
import art.intel.soft.databinding.ViewGalleryToolBarBinding
import art.intel.soft.utils.throttleClick

class GalleryToolBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var currentState = State.SINGLE_CHOICE
    private val folders = mutableListOf<String>()

    private val binding: ViewGalleryToolBarBinding =
            ViewGalleryToolBarBinding.inflate(LayoutInflater.from(context), this, true)

    private val popupMenu = PopupMenu(context, binding.folderButton)
    private var onFolderClickListener: ((String) -> Unit)? = null

    init {
        createPopupMenu()
        binding.folderButton.throttleClick { popupMenu.show() }
    }

    fun setTitle(titleText: String?) {
        binding.titleText.text = titleText ?: context.getString(currentState.defaultTitleId)
    }

    fun onBackClick(action: () -> Unit) = binding.backButton.throttleClick { action.invoke() }

    fun onApplyClick(action: () -> Unit) = binding.applyButton.throttleClick { action.invoke() }

    fun onCancelClick(action: () -> Unit) = binding.cancelButton.throttleClick { action.invoke() }

    fun onFolderClick(action: (String) -> Unit) {
        onFolderClickListener = action
    }

    fun setFolders(folders: List<String>) {
        popupMenu.menu.clear()

        this.folders.apply {
            clear()
            addAll(folders)
            forEach(popupMenu.menu::add)
        }
    }

    private fun createPopupMenu() = popupMenu.setOnMenuItemClickListener { item ->
        onFolderClickListener?.invoke(item.title.toString())

        true
    }

    fun showSelectableMode(isShow: Boolean) {
        binding.groupDefaultState.isVisible = !isShow
        binding.groupSelectableState.isVisible = isShow
    }

    fun setState(state: State) {
        currentState = state
        binding.titleText.text = context.getString(currentState.defaultTitleId)
    }

    enum class State(val defaultTitleId: Int) {
        SINGLE_CHOICE(R.string.gallery_title_one_choice),
        MULTI_CHOICE(R.string.gallery_title_many_choices),
    }
}
