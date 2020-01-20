package com.tencent.wmpf.demo.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.RequestsRepo

/**
 * Created by complexzeng on 2020-01-19 15:26.
 */
class GetDeviceInfoActivity : AppCompatActivity() {

    private val TAG = "GetDeviceInfoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_device_info)

        val appIdEditView = findViewById<EditText>(R.id.et_wxa_app_id)
        val ticketEditView = findViewById<EditText>(R.id.et_ticket)
        val kv = MMKV.mmkvWithID(TAG)
        if (!kv.getString("appId", "").isNullOrBlank() && !kv.getString("ticket", "").isNullOrBlank()) {
            appIdEditView.setText(kv.getString("appId", ""))
            ticketEditView.setText(kv.getString("ticket", ""))
        }
        val respTextView = findViewById<TextView>(R.id.tv_device_info_resp)

        findViewById<Button>(R.id.btn_get_device_info).setOnClickListener {
            kv.putString("appId", appIdEditView.text.toString())
            kv.putString("ticket", ticketEditView.text.toString())
            RequestsRepo.getTestDeviceInfo(ticketEditView.text.toString(), appIdEditView.text.toString(), DeviceInfo.APP_ID){
                respTextView.post {
                    respTextView.text = it
                }
            }
        }

    }
}