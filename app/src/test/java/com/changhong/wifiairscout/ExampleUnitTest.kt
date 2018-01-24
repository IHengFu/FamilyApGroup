package com.changhong.wifiairscout

import android.graphics.Rect
import org.junit.Test

import org.junit.Assert.*
import com.changhong.wifiairscout.ui.view.map.TypeMap
import com.changhong.wifiairscout.utils.CommUtils

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
//        val r1 = Rect(0, 0, 10, 10);
//        val r2 = Rect(9, 9, 11, 11)
//        System.err.println("a = " + r1.contains(r2))


        for (fl in TypeMap.RATE_WAVE) {

            System.out.println(fl.toString() + ":" + CommUtils.dbm2Distance(((App.MIN_RSSI - App.MAX_RSSI) * (1 - fl) + App.MAX_RSSI).toDouble()))
        }
    }
}
