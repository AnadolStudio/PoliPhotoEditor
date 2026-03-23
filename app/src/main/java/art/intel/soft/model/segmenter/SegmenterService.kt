package art.intel.soft.model.segmenter

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import art.intel.soft.extention.singleBy
import art.intel.soft.extention.singleFrom
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import io.reactivex.Single
import java.nio.ByteBuffer

class SegmenterService {

    companion object {
        private const val CONFIDENCE = 0.35f
        private const val MIN_CONFIDENCE_COUNT = 0.1f
    }

    fun createMask(bitmap: Bitmap): Single<SegmentationMask> = singleBy {
        val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()

        val image = InputImage.fromBitmap(bitmap, 0)
        Segmentation.getClient(options).process(image)
                .addOnSuccessListener(this::onSuccess)
                .addOnFailureListener(this::onError)
    }

    fun createBitmapByMask(
            mask: SegmentationMask,
            colorBackground: Int
    ): Single<Bitmap> = singleFrom {
        val maskBuffer: ByteBuffer = mask.buffer
        val width: Int = mask.width
        val height: Int = mask.height

        val size = width * height
        val bits = IntArray(size)

        var count = 0f

        for (index in 0 until height * width) {
            val foregroundConfidence = maskBuffer.float

            if (foregroundConfidence <= CONFIDENCE) {
                bits[index] = colorBackground
                count++
                continue
            }
        }

        if (1 - (count / size) <= MIN_CONFIDENCE_COUNT) throw SmallMaskException()

        return@singleFrom Bitmap.createBitmap(bits, width, height, ARGB_8888)
    }
}
