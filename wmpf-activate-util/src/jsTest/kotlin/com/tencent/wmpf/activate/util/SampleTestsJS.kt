package com.tencent.wmpf.activate.util

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTestsJS {
    @Test
    fun testHello() {
        assertTrue("JS" in getPlatformName())
    }
}