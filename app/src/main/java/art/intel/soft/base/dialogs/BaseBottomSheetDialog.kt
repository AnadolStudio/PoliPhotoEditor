package art.intel.soft.base.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog(private val layoutId: Int) : BottomSheetDialogFragment() {

    abstract fun getDialogTag(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle()
    }

    abstract fun setStyle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(layoutId, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            setDialogOptions(bottomSheet)
        }

        return bottomSheetDialog
    }

    fun show(fragmentManager: FragmentManager) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        val previous = fragmentManager.findFragmentByTag(getDialogTag())
        if (previous != null) fragmentTransaction.remove(previous)
        fragmentTransaction.addToBackStack(null)
        this.show(fragmentTransaction, getDialogTag())
    }

    protected fun setDialogOptions(
            bottomSheet: View?,
            viewOptionsInstaller: ((View) -> Unit)? = null
    ) {
        bottomSheet?.let { bottomSheetView ->
            viewOptionsInstaller?.invoke(bottomSheetView)
            BottomSheetBehavior.from(bottomSheetView).apply {
                skipCollapsed = true
                setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        }
    }

}
