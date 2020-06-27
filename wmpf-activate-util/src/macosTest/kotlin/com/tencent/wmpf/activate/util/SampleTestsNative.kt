package com.tencent.wmpf.activate.util

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTestsNative {
    @Test
    fun testHello() {
        assertTrue("Native" in getPlatformName())
    }
}