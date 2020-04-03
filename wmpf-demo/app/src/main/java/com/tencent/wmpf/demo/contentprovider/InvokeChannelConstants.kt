package com.tencent.wmpf.demo.contentprovider

import android.net.Uri

interface InvokeChannelConstants {

    interface ContentProvider {

        interface WMPF2Cli {
            companion object {
                const val SCHEME = "content"
                const val AUTHORITY = "com.tencent.wmpf.cli.provider"
                const val PATH_INVOKE_CHANNEL = "invokeChannel"
                const val PATH_INVOKE_CHANNEL_EVENT = "invokeChannelEvent"

                val URI_INVOKE_CHANNEL = Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(PATH_INVOKE_CHANNEL).build()
                val URI_INVOKE_CHANNEL_EVENT = Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(PATH_INVOKE_CHANNEL_EVENT).build()
            }
        }

        interface Cli2WMPF {
            companion object {
                const val SCHEME = "content"
                const val AUTHORITY = "com.tencent.wmpf.comm.provider"
                const val PATH_CALLBACK_INVOKE_CHANNEL = "callbackInvokeChannel"
                const val PATH_NOTIFY_INVOKE_CHANNEL_EVENT = "notifyInvokeChannelEvent"

                val URI_CALLBACK_INVOKE_CHANNEL = Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(PATH_CALLBACK_INVOKE_CHANNEL).build()
                val URI_NOTIFY_INVOKE_CHANNEL_EVENT = Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(PATH_NOTIFY_INVOKE_CHANNEL_EVENT).build()
            }
        }
    }

    interface Key {
        companion object {
            const val APP_ID = "appId"
            const val CALLBACK_ID = "callbackId"
            const val COMMAND = "command"
            const val DATA = "data"
            const val EVENT = "event"
            const val INVOKE_ID = "__invoke_id__"
            const val EVENT_ID = "__event_id__"
        }
    }
}
