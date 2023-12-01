package com.tencent.wmpf.demo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFAccountApi
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.api.WMPFMiniProgramApi
import com.tencent.wmpf.cli.api.WMPFMusicController
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.cli.model.protocol.WMPFStartAppRequest
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.Cgi
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoUtil
import com.tencent.wmpf.demo.utils.WMPFDemoUtil.execute

class DetailActivity : AppCompatActivity() {
    private var userInfoTextView: TextView? = null
    private var avatarImageView: ImageView? = null
    private var musicController = WMPFMusicController()
    private fun showOk(apiName: String) {
        val message = "$apiName success"
        Log.i(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFail(apiName: String, e: Exception) {
        val message = "$apiName fail: ${e.message}"
        Log.e(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun invokeWMPFApi(name: String, runnable: Runnable) {
        execute {
            try {
                runnable.run()
                showOk(name)
            } catch (e: WMPFApiException) {
                // WMPF 主动抛出的异常
                showFail(name, e)
            } catch (e: Exception) {
                showFail(name, e)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userInfoTextView = findViewById(R.id.userInfoTextView)
        avatarImageView = findViewById(R.id.avatarImageView)

        // Step 1.1
        findViewById<Button>(R.id.btn_init_wmpf_activate_device).setOnClickListener {
            invokeWMPFApi("activateDevice") {
                WMPF.getInstance().deviceApi.activateDevice()
            }
        }

        // Step 1.2
        findViewById<Button>(R.id.btn_init_wmpf).setOnClickListener {
            invokeWMPFApi("login") {
                val oauthCode =
                    WMPF.getInstance().accountApi.login(WMPFAccountApi.WMPFLoginUIStyle.FULLSCREEN)
                Log.i(TAG, "Login oauthCode: $oauthCode")
                runOnUiThread {
                    userInfoTextView!!.text = "Login"

                }
                if (oauthCode != null) {
                    val authInfo = Cgi.getOAuthInfo(
                        BuildConfig.HOST_APPID, BuildConfig.HOST_APPSECRET, oauthCode
                    )
                    val userInfo = Cgi.getUserInfo(BuildConfig.HOST_APPID, authInfo.accessToken)
                    Log.d(TAG, "userInfo: $userInfo")
                    runOnUiThread {
                        updateUserInfo(userInfo)
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_wmpf_runtime_preload).setOnClickListener {
            invokeWMPFApi("preload") {
                WMPF.getInstance().miniProgramApi.preload(null)
            }
        }

        findViewById<Button>(R.id.btn_warm_launch).setOnClickListener {
            invokeWMPFApi("warmUpApp") {
                val startParams = WMPFStartAppParams(
                    DEMO_APP_ID, "", WMPFStartAppParams.WMPFAppType.APP_TYPE_RELEASE
                )
                WMPF.getInstance().miniProgramApi.warmUpApp(startParams)
                runOnUiThread {
                    AlertDialog.Builder(this).setTitle("预热完成")
                        .setNegativeButton("启动小程序") { _, _ ->
                            invokeWMPFApi("launchMiniProgram") {
                                WMPF.getInstance().miniProgramApi.launchMiniProgram(startParams)
                            }
                        }.show()
                }
            }
        }

        // Step 2.1
        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            val startParams = WMPFStartAppParams(
                DEMO_APP_ID, "", WMPFStartAppParams.WMPFAppType.APP_TYPE_RELEASE
            )

            AlertDialog.Builder(this).setNegativeButton("normal") { _, _ ->
                invokeWMPFApi("launchMiniProgram") {
                    WMPF.getInstance().miniProgramApi.launchMiniProgram(
                        startParams, false, WMPFMiniProgramApi.LandscapeMode.NORMAL
                    )
                }
            }.setNeutralButton("landscape compat") { _, _ ->
                invokeWMPFApi("launchMiniProgram") {
                    WMPF.getInstance().miniProgramApi.launchMiniProgram(
                        startParams, false, WMPFMiniProgramApi.LandscapeMode.LANDSCAPE_COMPAT
                    )
                }
            }.setPositiveButton("landscape") { _, _ ->
                invokeWMPFApi("launchMiniProgram") {
                    // 示例直接传 WMPFStartAppRequest
                    WMPF.getInstance().miniProgramApi.launchMiniProgram(WMPFStartAppRequest().apply {
                        this.params = startParams
                        this.landscapeMode = WMPFMiniProgramApi.LandscapeMode.LANDSCAPE
                    })
                }
            }.show()
        }

        // Step 2.2
        findViewById<Button>(R.id.btn_launch_wxa_app_by_target_path).setOnClickListener {
            invokeWMPFApi("launchMiniProgram") {
                WMPF.getInstance().miniProgramApi.launchMiniProgram(
                    WMPFStartAppParams(
                        DEMO_APP_ID,
                        "packageComponent/pages/view/view/view",
                        WMPFStartAppParams.WMPFAppType.APP_TYPE_RELEASE
                    )
                )
            }
        }

        // Step 2.2
        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan).setOnClickListener {
            // Start wxa app by scan
            // U also can use scan invoker, contain scan ui
            LaunchWxaAppByScanInvoker.launchWxaByScanUI(this)
        }

        musicController.addMusicPlayStatusListener {
            // 在这里监听 newStatus
            Log.i(TAG, "music state: $it")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!WMPFDemoUtil.isLessThanWMPF22(application)) {
            menu?.add(0, 0, 0, "背景音频管理")
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.i(TAG, "menuItem id" + item?.itemId)
        return when (item?.itemId) {
            0 -> {
                invokeWMPFApi("showManageUI") {
                    WMPF.getInstance().musicApi.showManageUI()
                }
                true
            }

            else -> {
                false
            }
        }
    }

    private fun updateUserInfo(userInfo: Cgi.UserInfo) {
        userInfoTextView!!.text = String.format(
            "openId:%s\nnick: %s\nsex: %d\nprovince: %s\ncity: %s\ncountry: %s\nunionId: %s\ndeviceId:%s",
            userInfo.openId,
            userInfo.nickname,
            userInfo.sex,
            userInfo.province,
            userInfo.city,
            userInfo.country,
            userInfo.unionId,
        )



        Glide.with(this).load(userInfo.avatarUrl).into(avatarImageView!!)
    }

    companion object {
        private const val TAG = "WMPF.Demo.MainActivity"
        private const val DEMO_APP_ID = "wxe5f52902cf4de896"
    }
}
