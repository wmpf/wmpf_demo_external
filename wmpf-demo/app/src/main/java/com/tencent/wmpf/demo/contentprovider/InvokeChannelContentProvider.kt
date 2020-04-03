package com.tencent.wmpf.demo.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.tencent.mm.opensdk.utils.Log

class InvokeChannelContentProvider : ContentProvider() {
    private val mHandler by lazy {
        Handler(context?.mainLooper)
    }

    private val mEventIdList = hashSetOf<String?>()

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        when(sURIMatcher.match(p0)) {
            // 异步方法调用
            CODE_CALLBACK_INVOKE_CHANNEL -> {
                val invokeId = p1?.getAsString(InvokeChannelConstants.Key.INVOKE_ID)
                val command = p1?.getAsString(InvokeChannelConstants.Key.COMMAND)
                val sourceData = p1?.getAsString(InvokeChannelConstants.Key.DATA)
                Log.i(TAG, "invokeId: $invokeId, command: $command, sourceData: $sourceData")
                val cv = ContentValues()

                mHandler.postDelayed ({
                    cv.apply {
                        put(InvokeChannelConstants.Key.INVOKE_ID, invokeId)
                        put(InvokeChannelConstants.Key.COMMAND, command)
                        put(InvokeChannelConstants.Key.DATA, "invoke success")
                    }
                    try {
                        context?.contentResolver?.insert(InvokeChannelConstants.ContentProvider.Cli2WMPF.URI_CALLBACK_INVOKE_CHANNEL, cv)
                    } catch (e: Exception) {
                        Log.e(TAG, "callback invoke channel error")
                    }
                }, 1000)
            }
            // 回调事件注册
            CODE_NOTIFY_INVOKE_CHANNEL_EVENT -> {
                val eventId = p1?.getAsString(InvokeChannelConstants.Key.EVENT_ID)
                val event = p1?.getAsString(InvokeChannelConstants.Key.EVENT)
                Log.i(TAG, "register, eventId: $eventId, event: $event")

                mEventIdList.add(eventId)
                notifyEvent(0, eventId, event)
            }
        }
        return null
    }

    // 同步方法调用
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        Log.i(TAG, "method: $method, arg: $arg, extras: $extras")
        val bundle = Bundle()
        bundle.putString("data", "call success, method: $method, arg: $arg, extras: $extras")
        return bundle
    }

    override fun query(p0: Uri, p1: Array<String>?, p2: String?, p3: Array<String>?, p4: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<String>?): Int {
        return 0
    }

    // 回调事件反注册
    override fun delete(p0: Uri, p1: String?, p2: Array<String>?): Int {
        if (p2?.size == 2) {
            val eventId = p2[0]
            val event = p2[1]
            mEventIdList.remove(eventId)
            Log.i(TAG, "unregister success, eventId: $eventId, event: $event")
        }
        return 0
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    private fun notifyEvent(counter: Int, eventId: String?, event: String?) {
        val cv = ContentValues()
        mHandler.postDelayed ({
            if (!mEventIdList.contains(eventId)) {
                return@postDelayed
            }
            cv.apply {
                put(InvokeChannelConstants.Key.EVENT_ID, eventId)
                put(InvokeChannelConstants.Key.EVENT, event)
                put(InvokeChannelConstants.Key.DATA, "event$counter success")
            }
            try {
                context.contentResolver.insert(InvokeChannelConstants.ContentProvider.Cli2WMPF.URI_NOTIFY_INVOKE_CHANNEL_EVENT, cv)
                Log.i(TAG, "send message success, content: event$counter success")
            } catch (e: Exception) {
                Log.e(TAG, "callback invoke channel error")
            }
            if (counter < 10) {
                val i = counter + 1
                notifyEvent(i, eventId, event)
            }
        }, 3000)
    }

    companion object {
        private const val TAG = "InvokeChannelContentProvider"
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        private const val CODE_CALLBACK_INVOKE_CHANNEL = 1
        private const val CODE_NOTIFY_INVOKE_CHANNEL_EVENT = 2

        init {
            sURIMatcher.addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL, CODE_CALLBACK_INVOKE_CHANNEL)
            sURIMatcher.addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL_EVENT, CODE_NOTIFY_INVOKE_CHANNEL_EVENT)
        }
    }
}

