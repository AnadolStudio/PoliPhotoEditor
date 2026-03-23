package art.intel.soft.ui.edit.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditMainBinding
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.main.recycler.FunctionItem
import art.intel.soft.ui.edit.main.recycler.FunctionListAdapter
import art.intel.soft.ui.main.OpenEditType
import art.intel.soft.view.BaseToolbar
import kotlin.LazyThreadSafetyMode.NONE

class MainEditFragment : BaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_main_title

    private var listener: Action<FunctionItem>? = null // TODO to imlement by activity
    private var needNavigate = false
    private val binding by lazy(NONE) { FragmentEditMainBinding.inflate(layoutInflater) }

    companion object {
        private val allowedFunctions = setOf(
                FunctionItem.IMPROVE,
                FunctionItem.FILTER,
                FunctionItem.EFFECT,
                FunctionItem.TEXT,
                FunctionItem.STICKER,
                FunctionItem.CROP,
                FunctionItem.BRUSH,
                FunctionItem.BACKGROUND,
        )

        fun newInstance(
                type: OpenEditType,
                detailableListener: Action<FunctionItem>
        ): MainEditFragment = MainEditFragment().apply {
            listener = detailableListener
            arguments = Bundle().apply { putSerializable(OpenEditType::class.java.name, type) }
        }
    }

    override fun provideScreenName(): Screens = Screens.EDIT_MENU

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = binding.root.also { initView() }

    private fun initView() {
        val type = arguments?.getSerializable(OpenEditType::class.java.name) as? OpenEditType
        binding.mainRv.adapter = FunctionListAdapter(
                data = FunctionItem.values().filter { it in allowedFunctions },
                detailable = { item ->
                    listener?.action(item)
                    // TODO item.name on obfuscate
                    AnalyticEventsUtil.logEvent(OpenItemEvent.EditMenu(item.name))
                }
        )
        needNavigate = type != OpenEditType.EDIT_MENU

        editor().setBitmapReadyListener(this::navigateToFunction)
    }

    private fun navigateToFunction() {
        needNavigate = false

        val type = arguments?.getSerializable(OpenEditType::class.java.name) as? OpenEditType

        when (type) {
            OpenEditType.BACKGROUND -> listener?.action(FunctionItem.BACKGROUND)
            else -> Unit
        }
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction { editor().trySaveImage() }
    }

    override fun onBackPressed(): Boolean {
        if (editor().hasChanges) {
            showCancelDialog()
        } else {
            navigationUp()
        }

        return false
    }

    private fun navigationUp() {
        (requireActivity() as? AppCompatActivity)?.onSupportNavigateUp()
    }

    override fun dialogActionApply() = Unit

    override fun dialogActionNavigateBack() = navigationUp()
}
