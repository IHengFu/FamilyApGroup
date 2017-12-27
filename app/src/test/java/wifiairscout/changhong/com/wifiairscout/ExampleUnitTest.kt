package wifiairscout.changhong.com.wifiairscout

import android.graphics.Rect
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
val r1 = Rect(0,0,10,10);
        val r2 = Rect(9,9,11,11)
        System.err.println(r1.contains(r2))
    }
}
