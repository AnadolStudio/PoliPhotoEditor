package art.intel.soft.extention

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

fun <F : Fragment> F.withArgs(action: Bundle.() -> Unit): F = this.apply { arguments = Bundle().also(action) }

fun Fragment.compatDrawable(@DrawableRes id: Int): Drawable = requireContext().compatDrawable(id)
