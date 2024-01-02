package com.tencent.wmpf.demo.ui

import android.os.Bundle
import android.widget.Button
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFLifecycleListener
import com.tencent.wmpf.cli.event.AbstractOnPushMsgEventListener
import com.tencent.wmpf.cli.event.WMPFPushMsgData
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoUtil
import org.json.JSONObject


class VoipActivity : ApiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voip)

        findViewById<Button>(R.id.btn_listen).setOnClickListener {
            invokeWMPFApi("registerPushMsgEventListener") {
                setupPushCallback()
                showOk("监听成功")
            }
        }
    }

    private var hasPushCallback = false
    private fun setupPushCallback() {
        if (hasPushCallback) return
        if (WMPFDemoUtil.isLessThanWMPF22(application)) {
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
        val msgObject = JSONObject(msg)
        val path = msgObject.optString("path", "")
        val appType = msgObject.optInt("appType", 0)
        val appid = msgObject.optString("appid", "")
        // 暂且假设插件页启动代表 VoIP，wxf830863afde621eb 是插件 appId
        if (!path.contains("wxf830863afde621eb") || appid.isEmpty()) {
            return
        }
        showOk("收到来电，正在打开小程序")
        invokeWMPFApi("launchMiniProgram") {
            WMPF.getInstance().miniProgramApi.launchMiniProgram(
                WMPFStartAppParams(
                    appid, path, WMPFStartAppParams.WMPFAppType.values()[appType]
                )
            )
        }
    }
}