package art.intel.soft.ui.edit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import art.intel.soft.R
import art.intel.soft.base.BaseAction
import art.intel.soft.base.OnBackPressed
import art.intel.soft.base.OnBackPressedContainer
import art.intel.soft.base.dialogs.ApplyBottomDialog
import art.intel.soft.base.dialogs.CancelBottomDialog
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.ui.dialogs.imlemetation.LoadingDialog
import art.intel.soft.utils.bitmaputils.cropFromSource
import art.intel.soft.utils.bitmaputils.getXSpace
import art.intel.soft.utils.bitmaputils.getYSpace
import art.intel.soft.view.BaseToolbar

abstract class BaseEditFragment : Fragment(), IProcessEdit, OnBackPressed, CancelBottomDialog.Action,
        ApplyBottomDialog.Action {

    protected abstract val toolbarTitleId: Int?

    var callback: FragmentCreatedCallback? = null

    protected var hasChanges = false
        private set

    protected fun setChanges() {
        hasChanges = true
        onChangeAction(hasChanges)
    }

    protected fun clearChanges() {
        hasChanges = false
        onChangeAction(hasChanges)
    }

    protected open fun onChangeAction(hasChanges: Boolean) = Unit

    open fun nothingIsSelectedToast() = showToast(getString(R.string.edit_error_nothing_selected))

    protected fun showToast(@StringRes stringId: Int, duration: Int = LENGTH_SHORT) =
            showToast(getString(stringId), duration)

    protected fun showToast(text: String, duration: Int = LENGTH_SHORT) =
            makeText(context, text, duration).show()

    override fun process(main: Bitmap, support: Bitmap?): Bitmap =
            with(Bitmap.createBitmap(main.width, main.height, Bitmap.Config.ARGB_8888)) {

                val canvas = Canvas(this)
                canvas.drawBitmap(main, 0f, 0f, null)

                support?.also {
                    canvas.drawBitmap(
                            cropFromSource(
                                    main.width, main.height, getXSpace(main, it), getYSpace(main, it), it
                            ),
                            0f,
                            0f,
                            null
                    )
                    it.recycle()
                }
                main.recycle()
                this
            }

    fun editor(): EditActivity.EditViewHelper = editActivity().editHelper

    protected fun editActivity(): EditActivity = (activity as? EditActivity)
            ?: throw IllegalArgumentException("EditFragment used by non EditActivity")

    override fun onStart() {
        super.onStart()
        onSetupToolbar(editor().toolbar())
    }

    protected open fun onSetupToolbar(toolbar: BaseToolbar) {
        toolbarTitleId
                ?.let(this::getString)
                ?.let(toolbar::setTitle)

        toolbar.setLeftButtonAction { requireActivity().onBackPressed() }
        toolbar.setRightButtonAction { /*For clear memory leaks*/ }
    }

    override fun onResume() {
        super.onResume()
        editActivity()
        callback?.fragmentCreated().also { callback = null }
    }

    fun showLoadingDialog() {
        LoadingDialog().show(requireActivity().supportFragmentManager, LOADING_DIALOG_TAG)
    }

    fun hideLoadingDialog() {
        (requireActivity().supportFragmentManager.findFragmentByTag(LOADING_DIALOG_TAG) as? LoadingDialog)?.dismiss()
    }

    fun callOnBackPressedContainer(listener: OnBackPressed) {
        (requireActivity() as? OnBackPressedContainer)?.onBackPressed(listener)
    }

    protected fun loadRewarded() = Unit

    protected fun showRewarded(showedAction: BaseAction, action: BaseAction) = action.invoke()

    protected open fun onShowedRewarded() = Unit

    override fun dialogActionApply() = editor().applyProcess()

    override fun dialogActionNavigateBack() = callOnBackPressedContainer(this)

    override fun onBackPressed(): Boolean = false.also { showCancelDialog() }

    abstract fun provideScreenName(): Screens

    protected fun showCancelDialog() = CancelBottomDialog.newInstance(provideScreenName()).show(childFragmentManager)

    protected fun showApplyDialog() = ApplyBottomDialog.newInstance(provideScreenName()).show(childFragmentManager)

    companion object {
        const val REQUEST_CHOOSE_PHOTO = 1001
        const val REQUEST_CHOOSE_PHOTO_WITHOUT_AD = 1002
        const val CHOOSE_PHOTO_KEY = "choose_photo_key"
        const val CHOOSE_PHOTO = "choose_photo"
        const val CHOOSE_PHOTO_WITHOUT_AD = "CHOOSE_PHOTO_WITHOUT_AD"

        const val COLOR = "color"
        const val GRAVITY = "gravity"
        const val CURRENT_SIZE = "current_size"
        const val CURRENT_ALPHA = "current_alpha"
        private const val LOADING_DIALOG_TAG = "LoadingDialog"
    }

}
