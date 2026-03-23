package art.intel.soft.ui.edit.crop

import android.app.Application
import art.intel.soft.base.firebase.AnalyticEventFabric
import art.intel.soft.base.firebase.RememberItemDelegate
import art.intel.soft.base.view_model.BaseAndroidViewModel

class CropViewModel(
        application: Application,
) : BaseAndroidViewModel<Unit>(application),
        RememberItemDelegate<String> by RememberItemDelegate.Delegate(AnalyticEventFabric.Crop())
