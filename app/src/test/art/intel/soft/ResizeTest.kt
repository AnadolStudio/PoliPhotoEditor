package art.intel.soft

import art.intel.soft.utils.bitmaputils.scaleRatioCircumscribed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResizeTest {

    @Test
    fun test() {
        val scale = scaleRatioCircumscribed(666, 588, 336, 448)
        assertEquals(666F, 336 * scale)
        assertTrue(448 * scale >= 588)
    }

    @Test
    fun test1() {
        val scale = scaleRatioCircumscribed(662, 593, 600, 800)
        assertEquals(662F, 600 * scale)
        assertTrue(800 * scale >= 593)
    }
}
