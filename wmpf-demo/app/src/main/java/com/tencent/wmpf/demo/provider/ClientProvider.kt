package com.tencent.wmpf.demo.provider

import android.os.Handler
import android.util.Log
import com.tencent.wmpf.app.WMPFClientProvider
import com.tencent.wmpf.app.WMPFClientProvider.DeviceActivatedDelegate
import com.tencent.wmpf.app.WMPFClientProvider.InvokeChannelDelegate
import com.tencent.wmpf.utils.ExceptionalConsumer
import org.json.JSONObject

class ClientProvider : WMPFClientProvider(), InvokeChannelDelegate, DeviceActivatedDelegate {
    private lateinit var mHandler: Handler
    private val events = HashMap<String, String>()

    init {
        setDeviceActivatedDelegate(this)
        setInvokeChannelDelegate(this)
    }

    override fun onCreate(): Boolean {
        mHandler = Handler(context!!.mainLooper)
        return true // 直接返回 true 即可。
    }

    override fun onDeviceActivated() {
        // WMPF 通知设备已经激活成功，一般仅车机自主激活时需要使用，开发者主动调用 activateDevice 的场景可忽略。
    }


    /**
     * 小程序内调用 wmpf.Channel.registerEvent 函数时会调用本函数。
     * 事件注册后，可以通过 WMPFClientProvider.notifyInvokeChannelEvent 通知小程序事件。
     * 开发者应对 event 和 eventId 的关系进行持久化存储，以保证应用重启后依然可以向小程序正常发送事件。
     * @param event 事件名称
     * @param eventId 事件 id ，作为后续通知回调的凭据
     */
    override fun registerEvent(event: String, eventId: String) {
        Log.i(TAG, "registerEvent: ${event}, eventId=[$eventId]")
        events[eventId] = event

        mHandler.postDelayed({
            try {
                val payload = JSONObject().put("event", event).put("eventId", eventId)
                notifyInvokeChannelEvent(eventId, event, payload.toString())
            } catch (e: Exception) {
                Log.e(TAG, "send event failed: " + e.message)
            }
        }, 3000)
    }

    /**
     * 小程序内调用 wmpf.Channel.unregisterEvent 函数时会调用本函数。
     * @param event 事件名称
     * @param eventId 事件 id
     */
    override fun unregisterEvent(event: String, eventId: String) {
        Log.i(TAG, "unregisterEvent: ${event}, eventId=[$eventId]")
        events.remove(eventId)
    }

    /**
     * 小程序中调用 wmpf.Channel.invoke 函数时会调用本函数。
     * @param method 小程序传入的指令名 (command)
     * @param args 小程序传入的指令参数 (data)
     * @param callback 向小程序返回结果的 callback
     */
    override fun invoke(
        method: String, args: String, callback: ExceptionalConsumer<String, out Exception>
    ) {
        Log.i(TAG, "invokeAsync: ${method}, args=[$args]")
        val result = JSONObject().put("isAsync", true).put("command", method).put("data", args)
        mHandler.postDelayed({
            try {
                callback.consume(result.toString())
            } catch (e: Exception) {
                Log.e(TAG, "invoke callback failed: " + e.message)
            }
        }, 1000)
    }

    /**
     * 小程序中调用 wmpf.Channel.invokeSync 函数时会调用本函数。
     * @param method 小程序传入的指令名 (command)
     * @param args 小程序传入的指令参数 (data)
     */
    override fun invokeSync(method: String, args: String): String {
        Log.i(TAG, "invokeSync: ${method}, args=[$args]")
        val result = JSONObject().put("isAsync", false).put("command", method).put("data", args)

        return result.toString()
    }

    companion object {
        private const val TAG = "ClientProvider"
    }
}