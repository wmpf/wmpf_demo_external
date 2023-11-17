package com.tencent.wmpf.demo.utils

import android.util.Log
import android.widget.TextView
import com.tencent.wmpf.cli.api.WMPFClientDefaultExecutor
import java.util.concurrent.Callable
import java.util.concurrent.Future

object WMPFDemoUtil {
    private const val TAG = "WMPF.DemoUtil"

    class TextValue(private val view: TextView, private val defaultVal: String = "") {
        override fun toString(): String {
            val v = view.text.toString()
            if (v.isNullOrBlank()) {
                return defaultVal
            }
            return v
        }
    }

    private val executor = WMPFClientDefaultExecutor()

    fun execute(runnable: Runnable) {
        execute(Callable<Void?> {
            try {
                runnable.run()
            } catch (throwable: Throwable) {
                Log.wtf(
                    TAG,
                    "failed: " + throwable.message,
                    throwable
                )
                throw IllegalStateException(throwable)
            }
            null
        })
    }

    fun <T> execute(callable: Callable<T>?): Future<T> {
        return executor.submit(callable)
    }
}