package com.tencent.wmpf.demo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFLifecycleListener
import com.tencent.wmpf.cli.event.AbstractOnPushMsgEventListener
import com.tencent.wmpf.cli.event.WMPFPushMsgData
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.Cgi
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

class PushMsgQuickStartActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PushMsgActivity"
    }

    private lateinit var logger: WMPFDemoLogger

    private val consoleView: TextView by lazy {
        findViewById(R.id.console)
    }

    private val appIdView: EditText by lazy {
        findViewById(R.id.et_app_id)
    }

    private val msgView: EditText by lazy {
        findViewById(R.id.et_msg)
    }

    private val emitterView: Button by lazy {
        findViewById(R.id.emitter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_msg_quick_start)
        logger = WMPFDemoLogger(TAG, this, consoleView)

        emitterView.setOnClickListener {
            val appId = appIdView.text.toString()
            val msg = msgView.text.toString()

            if (appId.isBlank()) {
                logger.e("appId can not be blank")
                return@setOnClickListener
            }

            WMPFDemoUtil.execute {
                start(appId, msg)
            }

        }
    }

    private var pushToken = ""
    private var accessToken = ""
    private fun start(appId: String, msg: String) {
        try {
            setupPushCallback()
            if (accessToken.isBlank()) {
                logger.i("**Test Only: 该示例没有维护状态, 不应该多次获取token**")
                logger.i("1. 获取access_token...")
                accessToken =
                    Cgi.getAccessToken(BuildConfig.HOST_APPID, BuildConfig.HOST_APPSECRET)
            }
            if (pushToken.isBlank()) {
                val res = WMPF.getInstance().deviceApi.getPushToken(appId)
                logger.i("accessToken=$accessToken")
                logger.i("2. 获取push_token...")
                pushToken = res.pushToken
                logger.i("pushToken=$accessToken, expireTime=${res.expireTimestamp}")
            }
            logger.i("3. 推送消息 ${msg}...")
            val pushRes = Cgi.postMsg(accessToken, pushToken, msg)
            logger.i("4. 推送请求成功：$pushRes")
        } catch (e: Exception) {
            logger.e("发送失败", e)
        }
    }

    private var hasPushCallback = false

    private fun setupPushCallback() {
        if (hasPushCallback) return
        if (WMPFDemoUtil.getWmpfVersionCode(application) < 9020001) {
            // 2.1 版本使用旧接口
            WMPF.getInstance().deviceApi.setPushMsgCallback {
                handlePushMsg(it)
            }
            WMPF.getInstance().addWMPFLifecycleListener(object : WMPFLifecycleListener {
                override fun onWMPFRestart() {
                    // WMPF 重启后需要重新绑定
                    WMPF.getInstance().deviceApi.setPushMsgCallback {
                        handlePushMsg(it)
                    }
                }
            })
        } else {
            // 高版本使用新接口
            WMPF.getInstance().deviceApi.registerPushMsgEventListener(object :
                AbstractOnPushMsgEventListener() {
                override fun onInvoke(data: WMPFPushMsgData) {
                    handlePushMsg(data.message)
                }
            })
            // 新接口 WMPF 重启后会自动重新绑定
        }
        hasPushCallback = true
    }

    private fun handlePushMsg(msg: String) {
        logger.i("收到推送消息: [$msg]")
    }
}
