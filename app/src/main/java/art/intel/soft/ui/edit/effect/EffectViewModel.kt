package art.intel.soft.ui.edit.effect

import android.app.Application
import art.intel.soft.base.firebase.AnalyticEventFabric
import art.intel.soft.base.firebase.RememberItemDelegate
import art.intel.soft.base.view_model.BaseAndroidViewModel

class EffectViewModel(
        application: Application,
) : BaseAndroidViewModel<Unit>(application),
        RememberItemDelegate<String> by RememberItemDelegate.Delegate(AnalyticEventFabric.Effect())
