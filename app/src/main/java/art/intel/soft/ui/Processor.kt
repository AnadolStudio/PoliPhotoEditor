package art.intel.soft.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import art.intel.soft.extention.singleFrom
import art.intel.soft.ui.edit.IProcessEdit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Processor(var listener: ProcessorListener) {

    var processEdit: IProcessEdit? = null

    @SuppressLint("CheckResult")
    fun process(main: Bitmap, support: Bitmap?) {
        val processEdit = this.processEdit ?: return // TODO need Exception

        singleFrom { processEdit.process(main, support) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { resultBitmap -> listener.onProcess(resultBitmap) },
                        { error -> error.printStackTrace() }
                )
    }
}

interface ProcessorListener {

    fun onProcess(result: Bitmap)
}
