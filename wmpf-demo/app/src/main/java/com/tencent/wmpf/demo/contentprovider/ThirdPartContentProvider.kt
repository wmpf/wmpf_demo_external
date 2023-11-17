package com.tencent.wmpf.demo.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast

import com.tencent.mm.opensdk.utils.Log
import com.tencent.wmpf.demo.contentprovider.ContentProvideConstants.InvokeChannelConstants
import com.tencent.wmpf.demo.contentprovider.ContentProvideConstants.SpeakerConstants
import com.tencent.wmpf.demo.utils.WMPFDemoUtil
import java.util.Vector

class ThirdPartContentProvider : ContentProvider() {
    private val mHandler by lazy {
        Handler(context?.mainLooper)
    }

    private val mSpeakerRecord: Vector<Int> = Vector()
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
            // speaker接口调用
            CODE_SPEAKER_REQUEST -> {
                speakHandler(p1)
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
        when (sURIMatcher.match(p0)) {
            // 通知wmpf来拿speaker的执行结果后，wmpf执行一次query，在这里吐给wmpf执行结果
            CODE_SPEAKER_RESPONSE -> {
                return buildCallbackCursor()
            }
        }
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

    private fun speakHandler(cv: ContentValues?) {
        cv?:return

        val appId = "replace with your invoke appId here"
        val timeStamp = cv.getAsLong(SpeakerConstants.Key.KEY_TIME_STAMP)
        val token = cv.getAsString(SpeakerConstants.Key.KEY_TOKEN)
        val tokenLocalGen= WMPFDemoUtil.getMD5String(appId + "_" + timeStamp)
        if (tokenLocalGen != token) {
            Log.e(TAG, "token invalid")
            runOnUiThread (Runnable {
                Toast.makeText(context, "printer: token invalid!", Toast.LENGTH_LONG).show()
            })
            return
        }
        // rawData为前端透传过来的自定义数据结构字符串，对应前端data的数据
        val rawData = cv.getAsString(SpeakerConstants.Key.KEY_RAW_DATA)
        val callbackId = cv.getAsInteger(SpeakerConstants.Key.KEY_ID)
        mSpeakerRecord.add(callbackId)

        runOnUiThread (Runnable {
            Toast.makeText(context, "speaker data: $rawData", Toast.LENGTH_LONG).show()
        })

        mHandler.postDelayed ({
            notifyHandledResult()
        }, 3000)
    }

    private fun notifyHandledResult() {
        // 通知wmpf以query的形式拿执行结果
        context?.contentResolver?.notifyChange(SpeakerConstants.ContentProvider.URI_RESPONSE_PATH, null)
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread().id == context?.mainLooper?.thread?.id) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }

    private fun buildCallbackCursor(): Cursor {
        val cursor = MatrixCursor(arrayOf("callbackId", "code"))
        val iterator = mSpeakerRecord.iterator()
        while (iterator.hasNext()) {
            val callbackId = iterator.next()
            cursor.addRow(arrayOf(callbackId, SpeakerConstants.Code.CODE_SUCCESS))
        }
        return cursor
    }

    companion object {
        private const val TAG = "InvokeChannelContentProvider"
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        private const val CODE_CALLBACK_INVOKE_CHANNEL = 1
        private const val CODE_NOTIFY_INVOKE_CHANNEL_EVENT = 2
        private const val CODE_SPEAKER_REQUEST = 3
        private const val CODE_SPEAKER_RESPONSE = 4

        init {
            sURIMatcher.apply {
                addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL, CODE_CALLBACK_INVOKE_CHANNEL)
                addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL_EVENT, CODE_NOTIFY_INVOKE_CHANNEL_EVENT)
                addURI(SpeakerConstants.ContentProvider.AUTHORITY, SpeakerConstants.ContentProvider.PATH_REQUEST, CODE_SPEAKER_REQUEST)
                addURI(SpeakerConstants.ContentProvider.AUTHORITY, SpeakerConstants.ContentProvider.PATH_RESPONSE, CODE_SPEAKER_RESPONSE)
            }
        }
    }
}

