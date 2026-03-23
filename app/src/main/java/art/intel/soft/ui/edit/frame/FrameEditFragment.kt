package art.intel.soft.ui.edit.frame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.BottomListLayoutBinding
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.getPathList
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.crop.CropViewModel
import art.intel.soft.utils.bitmaputils.scaleBitmap
import art.intel.soft.view.BaseToolbar

class FrameEditFragment : BaseEditFragment(), Action<String> {

    override val toolbarTitleId: Int = R.string.edit_frames_title

    companion object {

        fun newInstance(callback: FragmentCreatedCallback?): FrameEditFragment = FrameEditFragment().apply {
            this.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.FRAMES
    private val viewModel: FrameViewModel by viewModels()

    private val binding by lazy(LazyThreadSafetyMode.NONE) { BottomListLayoutBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val thumbnail = scaleBitmap(400f, 400f, editor().currentBitmap(), false)

        val frames = mutableListOf<String?>().apply {
            add(0, null)
            addAll(getPathList(requireContext(), AssetsDirections.FRAMES_DIR))
        }

        binding.recyclerView.adapter = FrameDslAdapter(
                paths = frames,
                thumbnail = thumbnail,
                onSelect = { _, frame -> action(frame) }
        ).setup()

        return binding.root
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction(this::applyChanges)
    }

    private fun applyChanges() {
        if (!hasChanges) {
            nothingIsSelectedToast()

            return
        }

        showApplyDialog()
    }

    override fun dialogActionApply() {
        super.dialogActionApply()
        viewModel.applyItem()
    }

    override fun onBackPressed(): Boolean = when {
        !hasChanges -> true
        else -> false.also { showCancelDialog() }
    }

    override fun action(path: String?) {
        when (path != null) {
            true -> setChanges()
            false -> clearChanges()
        }

        path?.let(viewModel::onOpenItem)

        editor().setFrameImage(path)
    }
}
