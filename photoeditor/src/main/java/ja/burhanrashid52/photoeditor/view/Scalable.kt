package ja.burhanrashid52.photoeditor.view

import android.view.View

interface Scalable {

    fun setSupportScale(scale:Float)

    fun getScalableViews(): List<View>

}